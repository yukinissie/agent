# Local Development Environment

This directory contains Kubernetes deployment configurations for local development using Skaffold and Helm.

## Prerequisites

- Docker
- Kubernetes cluster (Docker Desktop, minikube, etc.)
- Skaffold
- Helm
- kubectl

## Services

- **example-api**: Rust Axum API (port 3000)
- **agent-api**: Clojure Ring API (port 3000)

## Quick Start

```bash
# Start both APIs
make dev

# Start agent-api for example-api development
make dev-example

# Start example-api for agent-api development
make dev-agent
```

## Available Commands

| Command | Description |
|---------|-------------|
| `make help` | Show available commands |
| `make dev` | Start both APIs with port forwarding |
| `make dev-example` | Start agent-api for example-api development |
| `make dev-agent` | Start example-api for agent-api development |
| `make build` | Build all Docker images |
| `make deploy` | Deploy to Kubernetes without port forwarding |
| `make delete` | Delete all deployments |
| `make clean` | Clean up resources and Docker images |

## Port Forwarding

When using `make dev`:
- **example-api**: http://127.0.0.1:3000
- **agent-api**: http://127.0.0.1:3001

When using individual profiles:
- `make dev-example`: agent-api runs on http://127.0.0.1:3000
- `make dev-agent`: example-api runs on http://127.0.0.1:3000

## API Endpoints

### example-api (Rust)
- `GET /health` - Health check
- `GET /users` - List users
- `GET /users/:id` - Get user by ID
- `POST /users` - Create user

### agent-api (Clojure)
- `GET /health` - Health check
- `GET /agents` - List agents
- `GET /agents/:id` - Get agent by ID

## Development Workflow

1. Make changes to your code
2. Skaffold will automatically detect changes and rebuild/redeploy
3. Access your APIs via the forwarded ports
4. Press Ctrl+C to stop

## Troubleshooting

If you encounter issues:

1. Check cluster connection: `kubectl cluster-info`
2. Clean up resources: `make clean`
3. Rebuild images: `make build`

## Directory Structure

```
agent-infra/local/
├── Makefile              # Development commands
├── README.md            # This file
├── skaffold.yaml        # Skaffold configuration
├── example-api/
│   └── helm/           # Helm chart for example-api
└── agent-api/
    └── helm/           # Helm chart for agent-api
```