terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.20"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.10"
    }
  }

  backend "s3" {
    # These values are loaded from .env file via terraform init -backend-config
    # bucket = var.TERRAFORM_STATE_BUCKET  # Set via -backend-config or env vars
    # key    = var.TERRAFORM_STATE_KEY     # Set via -backend-config or env vars  
    # region = var.TERRAFORM_STATE_REGION  # Set via -backend-config or env vars
  }
}