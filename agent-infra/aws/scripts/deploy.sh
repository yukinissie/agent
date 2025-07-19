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

# Get ECR repository URLs from environment variables or terraform output
if [ -z "$EXAMPLE_API_ECR_URL" ] || [ -z "$AGENT_API_ECR_URL" ]; then
    echo_info "ECR URLs not found in environment variables, getting from terraform output..."
    EXAMPLE_API_ECR_URL=$(cd terraform && terraform output -raw example_api_ecr_repository_url)
    AGENT_API_ECR_URL=$(cd terraform && terraform output -raw agent_api_ecr_repository_url)
fi

echo_info "Deploying applications to EKS..."
echo_info "Example API ECR: $EXAMPLE_API_ECR_URL"
echo_info "Agent API ECR: $AGENT_API_ECR_URL"

# Create temporary values files with substituted environment variables
echo_info "Creating temporary values files with environment variables..."
envsubst < k8s/example-api/helm/values-prod.yaml > /tmp/example-api-values.yaml
envsubst < k8s/agent-api/helm/values-prod.yaml > /tmp/agent-api-values.yaml

# Check if Helm is installed
if ! command -v helm &> /dev/null; then
    echo_error "Helm is required but not installed. Please install Helm first."
    exit 1
fi

# Deploy example-api
echo_info "Deploying example-api..."
helm upgrade --install example-api ../local/example-api/helm \
  --values /tmp/example-api-values.yaml \
  --namespace default \
  --wait

# Deploy agent-api
echo_info "Deploying agent-api..."
helm upgrade --install agent-api ../local/agent-api/helm \
  --values /tmp/agent-api-values.yaml \
  --namespace default \
  --wait

# Wait for deployments to be ready
echo_info "Waiting for deployments to be ready..."
kubectl wait --for=condition=available deployment/example-api --timeout=300s
kubectl wait --for=condition=available deployment/agent-api --timeout=300s

# Show deployment status
echo_info "Deployment status:"
kubectl get pods -l app.kubernetes.io/name=example-api
kubectl get pods -l app.kubernetes.io/name=agent-api

echo_info "Getting ingress information..."
kubectl get ingress

echo_info "Applications deployed successfully!"
echo_info "To access the applications, use the ALB URLs from the ingress above."

# Clean up temporary files
rm -f /tmp/example-api-values.yaml /tmp/agent-api-values.yaml