.PHONY: build-worker generate today deploy serve

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs --no-daemon -q

generate:
	./gradlew :generator:run --no-daemon -q

today:
	./gradlew :generator:new --no-daemon -q

deploy: generate build-worker
	npx cf deploy

serve:
	npx cf dev
