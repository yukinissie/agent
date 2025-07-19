.PHONY: test build clean help

# Default target
help:
	@echo "Available commands:"
	@echo "  make test   - Compile Kotlin and run Gauge tests"
	@echo "  make build  - Compile Kotlin code only"
	@echo "  make clean  - Clean build artifacts"
	@echo "  make help   - Show this help message"

# Build Kotlin code and copy dependencies
build:
	./gradlew prepareGauge

# Run tests (build first, then run gauge)
test: build
	gauge run specs

# Clean build artifacts
clean:
	./gradlew clean
	rm -rf gauge_bin logs reports
