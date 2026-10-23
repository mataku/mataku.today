.PHONY: build-worker generate today deploy serve

build-worker:
	./gradlew :worker:compileProductionExecutableKotlinJs --no-daemon -q

generate:
	./gradlew :generator:run --no-daemon -q

today:
	kotlin scripts/new-article.main.kts

deploy: generate build-worker
	npx cf deploy

serve:
	npx cf dev
