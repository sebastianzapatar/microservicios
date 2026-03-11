#!/usr/bin/env bash
# build-images.sh — builds all local Spring Boot Docker images
# Usage: bash k8s/build-images.sh [--minikube]
# Pass --minikube if using minikube so images are loaded into the cluster.

set -e

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
USE_MINIKUBE=false

for arg in "$@"; do
  case $arg in
    --minikube) USE_MINIKUBE=true ;;
  esac
done

SERVICES=(serversc eureka product-service order-service apigetaway)

echo "🚀 Building Docker images from: $ROOT"
echo ""

for svc in "${SERVICES[@]}"; do
  echo "──────────────────────────────────"
  echo "📦 Building image: $svc"
  docker build -t "$svc:latest" "$ROOT/$svc"
  echo "✅ $svc:latest built"

  if [ "$USE_MINIKUBE" = true ]; then
    echo "⬆️  Loading $svc:latest into minikube..."
    minikube image load "$svc:latest"
    echo "✅ $svc:latest loaded into minikube"
  fi
  echo ""
done

echo "══════════════════════════════════"
echo "All images built successfully!"
echo ""
echo "Next step:"
if [ "$USE_MINIKUBE" = true ]; then
  echo "  kubectl apply -k k8s/"
else
  echo "  kubectl apply -k k8s/"
  echo ""
  echo "  If using Docker Desktop, make sure Kubernetes is enabled in:"
  echo "  Docker Desktop → Settings → Kubernetes → Enable Kubernetes"
fi
