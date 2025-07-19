#!/bin/bash

set -e

# Load environment variables from .env file
if [ -f ".env" ]; then
    source .env
fi

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

# Get cluster name and region from environment variables or terraform output
if [ -z "$CLUSTER_NAME" ] || [ -z "$AWS_REGION" ]; then
    echo_info "Cluster info not found in environment variables, getting from terraform output..."
    CLUSTER_NAME=${CLUSTER_NAME:-$(cd terraform && terraform output -raw cluster_name)}
    AWS_REGION=${AWS_REGION:-$(cd terraform && terraform output -raw aws_region || echo "us-west-2")}
fi

echo_info "Setting up EKS cluster: $CLUSTER_NAME in region: $AWS_REGION"

# Update kubeconfig
echo_info "Updating kubeconfig..."
aws eks update-kubeconfig --region $AWS_REGION --name $CLUSTER_NAME

# Verify cluster connection
echo_info "Verifying cluster connection..."
if ! kubectl cluster-info >/dev/null 2>&1; then
    echo_error "Failed to connect to cluster"
    exit 1
fi

# Install cert-manager (required for ALB controller)
echo_info "Installing cert-manager..."
kubectl apply --validate=false -f https://github.com/jetstack/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Wait for cert-manager to be ready
echo_info "Waiting for cert-manager to be ready..."
kubectl wait --for=condition=ready pod -l app=cert-manager -n cert-manager --timeout=300s
kubectl wait --for=condition=ready pod -l app=cainjector -n cert-manager --timeout=300s
kubectl wait --for=condition=ready pod -l app=webhook -n cert-manager --timeout=300s

echo_info "EKS cluster setup completed successfully!"
echo_info "Next steps:"
echo_info "  1. Run 'make install-alb-controller' to install the ALB controller"
echo_info "  2. Run 'make deploy' to deploy applications"