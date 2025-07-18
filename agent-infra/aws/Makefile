.PHONY: help init plan apply destroy login setup-cluster install-alb-controller deploy clean

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
	cd terraform && terraform init

plan:
	@echo "Planning Terraform changes..."
	cd terraform && terraform plan

apply:
	@echo "Applying Terraform changes..."
	cd terraform && terraform apply

destroy:
	@echo "Destroying infrastructure..."
	cd terraform && terraform destroy

# AWS/EKS commands
login:
	@echo "Logging into AWS ECR..."
	aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin $$(aws sts get-caller-identity --query Account --output text).dkr.ecr.us-west-2.amazonaws.com

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