.PHONY: build-worker generate today deploy

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs --no-daemon

generate:
	./gradlew :generator:run --no-daemon

today:
	./gradlew :generator:new --no-daemon

deploy: generate build-worker
	npx wrangler deploy
