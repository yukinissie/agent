#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

echo_warn "This will clean up all AWS resources created by Terraform."
echo_warn "This action cannot be undone!"
read -p "Are you sure you want to continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo_info "Cleanup cancelled."
    exit 0
fi

# Clean up Kubernetes resources first
echo_info "Cleaning up Kubernetes resources..."

# Delete applications
echo_info "Deleting applications..."
helm uninstall example-api --ignore-not-found || true
helm uninstall agent-api --ignore-not-found || true

# Delete ALB controller
echo_info "Deleting ALB controller..."
kubectl delete -f k8s/ingress/alb-ingress-controller.yaml --ignore-not-found || true

# Delete cert-manager
echo_info "Deleting cert-manager..."
kubectl delete -f https://github.com/jetstack/cert-manager/releases/download/v1.13.0/cert-manager.yaml --ignore-not-found || true

# Wait a bit for resources to be cleaned up
echo_info "Waiting for resources to be cleaned up..."
sleep 30

# Clean up Terraform resources
echo_info "Cleaning up Terraform resources..."
cd terraform && terraform destroy -auto-approve

# Clean up Docker images
echo_info "Cleaning up Docker images..."
docker system prune -f || true

echo_info "Cleanup completed!"
echo_info "All AWS resources have been destroyed."