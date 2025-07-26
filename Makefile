serve:
	npx wrangler pages dev functions/ && OGP_API_ENDPOINT="http://127.0.0.1:8788/ogp?url=" hugo server -D &

today:
	$(eval TODAY_FILE := content/$(shell date +%Y)/$(shell date +%m)/$(shell date +%d)/index.md)
	hugo new $(TODAY_FILE) && nvim $(TODAY_FILE)
