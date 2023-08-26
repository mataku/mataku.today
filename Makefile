serve:
	npx wrangler pages dev functions/ && OGP_API_ENDPOINT="http://127.0.0.1:8788/ogp?url=" hugo server -D &
