.PHONY: upload-r2 build-worker deploy-image generate today

upload-r2:
	@find output -type f | while read file; do \
		key=$${file#output/}; \
		npx wrangler r2 object put mataku-today/$$key --file=$$file --remote; \
	done

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs --no-daemon

deploy-image:
	@if [ -z "$(filter-out $@,$(MAKECMDGOALS))" ]; then \
		echo "Error: FILE is required. Usage: make deploy-image path/to/image.png"; \
		exit 1; \
	fi
	@npx wrangler r2 object put mataku-today/$(filter-out $@,$(MAKECMDGOALS)) --file=articles/$(filter-out $@,$(MAKECMDGOALS)) --remote

%:
	@:

generate:
	./gradlew :generator:run --no-daemon

today:
	./gradlew :generator:new --no-daemon
