#!/usr/bin/env bash
# =============================================================================
# setup.sh — Bootstrap script to initialize and deploy the AWS infrastructure
# Run this ONCE before using GitHub Actions CI/CD
#
# Requirements:
#   - AWS CLI configured (aws configure)
#   - Terraform >= 1.6 installed
#   - jq installed (brew install jq)
# =============================================================================

set -euo pipefail

# ─── Configuration ─────────────────────────────────────────────────────────────
AWS_REGION="us-east-1"
PROJECT_NAME="microservices"
BUCKET_NAME="${PROJECT_NAME}-terraform-state"
DYNAMO_TABLE="${PROJECT_NAME}-terraform-locks"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "╔══════════════════════════════════════════════╗"
echo "║  Microservices AWS Bootstrap                ║"
echo "║  Account: ${ACCOUNT_ID}                     ║"
echo "║  Region : ${AWS_REGION}                     ║"
echo "╚══════════════════════════════════════════════╝"

# ─── Step 1: Create S3 Bucket for Terraform State ──────────────────────────────
echo ""
echo "[1/5] Creating S3 bucket for Terraform state..."
if aws s3api head-bucket --bucket "${BUCKET_NAME}" 2>/dev/null; then
  echo "      ✅ Bucket already exists: ${BUCKET_NAME}"
else
  aws s3api create-bucket \
    --bucket "${BUCKET_NAME}" \
    --region "${AWS_REGION}"
  aws s3api put-bucket-versioning \
    --bucket "${BUCKET_NAME}" \
    --versioning-configuration Status=Enabled
  aws s3api put-bucket-encryption \
    --bucket "${BUCKET_NAME}" \
    --server-side-encryption-configuration '{
      "Rules": [{
        "ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"},
        "BucketKeyEnabled": true
      }]
    }'
  aws s3api put-public-access-block \
    --bucket "${BUCKET_NAME}" \
    --public-access-block-configuration "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
  echo "      ✅ Bucket created: ${BUCKET_NAME}"
fi

# ─── Step 2: Create DynamoDB Table for State Locking ──────────────────────────
echo ""
echo "[2/5] Creating DynamoDB table for state locking..."
if aws dynamodb describe-table --table-name "${DYNAMO_TABLE}" --region "${AWS_REGION}" 2>/dev/null; then
  echo "      ✅ DynamoDB table already exists: ${DYNAMO_TABLE}"
else
  aws dynamodb create-table \
    --table-name "${DYNAMO_TABLE}" \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region "${AWS_REGION}"
  echo "      ✅ DynamoDB table created: ${DYNAMO_TABLE}"
fi

# ─── Step 3: Create terraform.tfvars ──────────────────────────────────────────
echo ""
echo "[3/5] Checking terraform.tfvars..."
TERRAFORM_DIR="$(dirname "$0")/../terraform"
cd "${TERRAFORM_DIR}"

if [ ! -f terraform.tfvars ]; then
  cp terraform.tfvars.example terraform.tfvars
  echo "      ⚠️  Created terraform.tfvars from example."
  echo "      ➜  EDIT terraform.tfvars now and set your db_password, then re-run this script."
  echo "      ➜  File: ${TERRAFORM_DIR}/terraform.tfvars"
  exit 0
else
  echo "      ✅ terraform.tfvars already exists."
fi

# Check that db_password was changed from the default
if grep -q "CHANGE_ME" terraform.tfvars; then
  echo "      ❌ ERROR: You must change db_password in terraform.tfvars before continuing!"
  exit 1
fi

# ─── Step 4: Terraform Init + Plan ────────────────────────────────────────────
echo ""
echo "[4/5] Running terraform init and plan..."
terraform init -upgrade
terraform validate
terraform plan -out=tfplan

# ─── Step 5: Terraform Apply ──────────────────────────────────────────────────
echo ""
echo "[5/5] Applying Terraform..."
read -p "      Proceed with terraform apply? [y/N] " confirm
if [[ "${confirm}" == "y" || "${confirm}" == "Y" ]]; then
  terraform apply tfplan

  echo ""
  echo "════ Deployment Outputs ════════════════════════════════════"
  terraform output alb_url
  echo ""
  echo "💡 GitHub Actions Secrets (add to your repository):"
  echo "   AWS_ACCESS_KEY_ID     = $(terraform output -raw github_actions_access_key_id)"
  echo "   AWS_SECRET_ACCESS_KEY = $(terraform output -raw github_actions_secret_access_key)"
  echo "   AWS_ACCOUNT_ID        = ${ACCOUNT_ID}"
  echo "   AWS_REGION            = ${AWS_REGION}"
  echo ""
  echo "⚠️  Store these secrets securely. They will not be shown again."
  echo "════════════════════════════════════════════════════════════"
else
  echo "      Cancelled."
fi
