.PHONY: help dev dev-example dev-agent stop clean build deploy delete

# Default target
help:
	@echo "Available commands:"
	@echo "  make dev          - Start both APIs with port forwarding"
	@echo "  make dev-example  - Start agent-api for example-api development"
	@echo "  make dev-agent    - Start example-api for agent-api development"
	@echo "  make build        - Build all images without deploying"
	@echo "  make deploy       - Deploy both APIs to k8s"
	@echo "  make delete       - Delete all deployments"
	@echo "  make stop         - Stop skaffold (alias for Ctrl+C)"
	@echo "  make clean        - Clean up resources and images"

# Development with port forwarding
dev:
	@echo "Starting both APIs with port forwarding..."
	@echo "example-api: http://127.0.0.1:3000"
	@echo "agent-api: http://127.0.0.1:3001"
	@echo "Press Ctrl+C to stop"
	skaffold dev --port-forward

dev-example:
	@echo "Starting agent-api for example-api development..."
	@echo "agent-api: http://127.0.0.1:3000"
	@echo "Now you can run example-api locally for development"
	@echo "Press Ctrl+C to stop"
	skaffold dev -p agent-api --port-forward

dev-agent:
	@echo "Starting example-api for agent-api development..."
	@echo "example-api: http://127.0.0.1:3000"
	@echo "Now you can run agent-api locally for development"
	@echo "Press Ctrl+C to stop"
	skaffold dev -p example-api --port-forward

# Build only
build:
	@echo "Building all images..."
	skaffold build

# Deploy only
deploy:
	@echo "Deploying both APIs to Kubernetes..."
	skaffold deploy

# Delete deployments
delete:
	@echo "Deleting all deployments..."
	skaffold delete

# Clean up
clean:
	@echo "Cleaning up resources..."
	skaffold delete || true
	@echo "Cleaning up Docker images..."
	docker image prune -f
	@echo "Cleanup completed"

# Stop (informational)
stop:
	@echo "To stop skaffold, press Ctrl+C in the terminal where it's running"