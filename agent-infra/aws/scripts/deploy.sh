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

# Get ECR repository URLs from terraform output
EXAMPLE_API_ECR=$(cd terraform && terraform output -raw example_api_ecr_repository_url)
AGENT_API_ECR=$(cd terraform && terraform output -raw agent_api_ecr_repository_url)

echo_info "Deploying applications to EKS..."
echo_info "Example API ECR: $EXAMPLE_API_ECR"
echo_info "Agent API ECR: $AGENT_API_ECR"

# Check if Helm is installed
if ! command -v helm &> /dev/null; then
    echo_error "Helm is required but not installed. Please install Helm first."
    exit 1
fi

# Deploy example-api
echo_info "Deploying example-api..."
helm upgrade --install example-api ../local/example-api/helm \
  --values k8s/example-api/helm/values-prod.yaml \
  --set image.repository=$EXAMPLE_API_ECR \
  --set image.tag=latest \
  --namespace default \
  --wait

# Deploy agent-api
echo_info "Deploying agent-api..."
helm upgrade --install agent-api ../local/agent-api/helm \
  --values k8s/agent-api/helm/values-prod.yaml \
  --set image.repository=$AGENT_API_ECR \
  --set image.tag=latest \
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