# Dockerfile for CCA-MCP deployment to Amazon Bedrock AgentCore

FROM openjdk:17-jdk-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Install Leiningen
ENV LEIN_VERSION=2.10.0
ENV LEIN_INSTALL=/usr/local/bin/
WORKDIR $LEIN_INSTALL
RUN wget -q https://raw.githubusercontent.com/technomancy/leiningen/$LEIN_VERSION/bin/lein && \
    chmod 0755 lein && \
    ./lein version

# Set working directory
WORKDIR /app

# Copy project configuration
COPY project.clj ./

# Download dependencies
RUN lein deps

# Copy source code
COPY src/ src/

# Build uberjar
RUN lein uberjar

# Set environment variables for AgentCore
ENV PORT=4000
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose port
EXPOSE 4000

# Health check endpoint - MCP ping via JSON-RPC
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f -X POST -H "Content-Type: application/json" \
    -d '{"jsonrpc":"2.0","id":1,"method":"ping"}' \
    http://localhost:4000/ || exit 1

# Run the application
CMD ["java", "-jar", "target/cca-mcp-0.1.0-SNAPSHOT-standalone.jar"]
