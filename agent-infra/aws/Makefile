.PHONY: help init plan apply destroy login setup-cluster install-alb-controller deploy clean

# Load environment variables from .env file
ifneq (,$(wildcard ./.env))
include .env
export
endif

# Default target
help:
	@echo "Available commands:"
	@echo "  make init              - Initialize Terraform"
	@echo "  make plan              - Plan Terraform changes"
	@echo "  make apply             - Apply Terraform changes"
	@echo "  make destroy           - Destroy infrastructure"
	@echo "  make login             - Login to AWS ECR"
	@echo "  make setup-cluster     - Setup complete EKS cluster"
	@echo "  make install-alb-controller - Install AWS Load Balancer Controller"
	@echo "  make deploy            - Deploy applications to EKS"
	@echo "  make clean             - Clean up resources"

# Terraform commands
init:
	@echo "Initializing Terraform..."
	cd terraform && terraform init \
		-backend-config="bucket=${TERRAFORM_STATE_BUCKET}" \
		-backend-config="key=${TERRAFORM_STATE_KEY}" \
		-backend-config="region=${TERRAFORM_STATE_REGION}"

plan:
	@echo "Planning Terraform changes..."
	cd terraform && terraform plan \
		-var="project_name=${PROJECT_NAME}" \
		-var="environment=${ENVIRONMENT}" \
		-var="aws_region=${AWS_REGION}" \
		-var="cluster_name=${CLUSTER_NAME}" \
		-var="node_group_desired_size=${NODE_GROUP_DESIRED_SIZE}" \
		-var="node_group_max_size=${NODE_GROUP_MAX_SIZE}" \
		-var="node_group_min_size=${NODE_GROUP_MIN_SIZE}"

apply:
	@echo "Applying Terraform changes..."
	cd terraform && terraform apply \
		-var="project_name=${PROJECT_NAME}" \
		-var="environment=${ENVIRONMENT}" \
		-var="aws_region=${AWS_REGION}" \
		-var="cluster_name=${CLUSTER_NAME}" \
		-var="node_group_desired_size=${NODE_GROUP_DESIRED_SIZE}" \
		-var="node_group_max_size=${NODE_GROUP_MAX_SIZE}" \
		-var="node_group_min_size=${NODE_GROUP_MIN_SIZE}"

destroy:
	@echo "Destroying infrastructure..."
	cd terraform && terraform destroy

# AWS/EKS commands
login:
	@echo "Logging into AWS ECR..."
	aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

setup-cluster: apply
	@echo "Setting up EKS cluster..."
	./scripts/setup-cluster.sh

install-alb-controller:
	@echo "Installing AWS Load Balancer Controller..."
	./scripts/install-alb-controller.sh

deploy:
	@echo "Deploying applications..."
	./scripts/deploy.sh

clean:
	@echo "Cleaning up resources..."
	./scripts/cleanup.sh