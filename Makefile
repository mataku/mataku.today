.PHONY: build-worker generate dev today deploy serve

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs --no-daemon -q

generate:
	./gradlew :generator:run --no-daemon -q

dev:
	DEV=1 ./gradlew :generator:run -q

today:
	./gradlew :generator:new --no-daemon -q

deploy: generate build-worker
	npx wrangler deploy

serve:
	npx wrangler dev
