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

# Get values from environment variables or terraform output
CLUSTER_NAME=${CLUSTER_NAME:-$(cd terraform && terraform output -raw cluster_name)}
VPC_ID=${VPC_ID:-$(cd terraform && terraform output -raw vpc_id)}
AWS_REGION=${AWS_REGION:-$(cd terraform && terraform output -raw aws_region || echo "us-west-2")}
ACCOUNT_ID=${AWS_ACCOUNT_ID:-$(aws sts get-caller-identity --query Account --output text)}
ROLE_ARN=${ROLE_ARN:-$(cd terraform && terraform output -raw load_balancer_controller_role_arn)}

echo_info "Installing AWS Load Balancer Controller..."
echo_info "Cluster: $CLUSTER_NAME"
echo_info "VPC ID: $VPC_ID"
echo_info "Region: $AWS_REGION"
echo_info "Role ARN: $ROLE_ARN"

# Create modified ALB controller manifest
echo_info "Creating ALB controller manifest..."
cp k8s/ingress/alb-ingress-controller.yaml /tmp/alb-controller.yaml

# Replace placeholders  
sed -i "s/ACCOUNT_ID/$ACCOUNT_ID/g" /tmp/alb-controller.yaml
sed -i "s/VPC_ID/$VPC_ID/g" /tmp/alb-controller.yaml
sed -i "s/agent-prod/$CLUSTER_NAME/g" /tmp/alb-controller.yaml
sed -i "s/us-west-2/$AWS_REGION/g" /tmp/alb-controller.yaml

# Apply the manifest
echo_info "Applying ALB controller manifest..."
kubectl apply -f /tmp/alb-controller.yaml

# Wait for ALB controller to be ready
echo_info "Waiting for ALB controller to be ready..."
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=aws-load-balancer-controller -n kube-system --timeout=300s

# Verify installation
echo_info "Verifying ALB controller installation..."
kubectl get deployment -n kube-system aws-load-balancer-controller

echo_info "AWS Load Balancer Controller installed successfully!"

# Clean up
rm -f /tmp/alb-controller.yaml