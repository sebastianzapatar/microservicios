variable "aws_region" {
  description = "AWS region to deploy infrastructure"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project prefix used in resource names"
  type        = string
  default     = "microservices"
}

variable "environment" {
  description = "Deployment environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

# ─── VPC ───────────────────────────────────────────────────────────────────────
variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

# ─── RDS ───────────────────────────────────────────────────────────────────────
variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "postgres"
  sensitive   = true
}

variable "db_password" {
  description = "PostgreSQL master password (min 8 chars)"
  type        = string
  sensitive   = true
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

# ─── ECS ───────────────────────────────────────────────────────────────────────
variable "ecs_task_cpu" {
  description = "Default CPU units for ECS tasks (256 = 0.25 vCPU)"
  type        = number
  default     = 512
}

variable "ecs_task_memory" {
  description = "Default memory in MB for ECS tasks"
  type        = number
  default     = 1024
}

variable "services_desired_count" {
  description = "Number of desired tasks per microservice"
  type        = number
  default     = 1
}

# ─── ECR ───────────────────────────────────────────────────────────────────────
variable "ecr_image_tag_mutability" {
  description = "Image tag mutability for ECR: MUTABLE or IMMUTABLE"
  type        = string
  default     = "MUTABLE"
}

variable "ecr_image_retention_count" {
  description = "Number of images to retain in ECR lifecycle policy"
  type        = number
  default     = 5
}

# ─── TAGS ──────────────────────────────────────────────────────────────────────
variable "tags" {
  description = "Common tags applied to all resources"
  type        = map(string)
  default = {
    Project     = "microservices"
    ManagedBy   = "Terraform"
    Environment = "dev"
  }
}
