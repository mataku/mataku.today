# mataku.today

Personal blog built with Kotlin: a static site generator (JVM) and a Cloudflare Worker (Kotlin/JS) that serves the generated files via the Worker Assets binding.

## Commands

```shell
make generate        # Generate static HTML from markdown articles into output/
make build-worker    # Build the Cloudflare Worker
make deploy          # generate + build-worker + cf deploy
make today           # Create articles/YYYY/MM/DD/index.md (requires the kotlin CLI)
./gradlew :generator:feed  # Generate RSS feed (feed.xml)
```

## Architecture

### generator (`:generator`)

JVM application that converts Markdown articles to HTML.

- Entry point: `blog.MainKt`
- Markdown parsing: Commonmark with GFM extensions (tables, strikethrough, autolink)
- Embed transformers: X/Twitter, Gist, YouTube, Spotify, ImageCaption
- Copies `assets/` to `output/assets/` on every run
- Outputs to `output/`

### worker (`:worker`)

Kotlin/JS Cloudflare Worker.

- Entry point: `worker/entry.js` imports the compiled Kotlin/JS
- Routes requests and serves content from Worker Assets (`env.ASSETS`)
- Handles articles, assets, index, pagination, robots.txt, sitemap.xml, feed.xml, privacy_policy
- `run_worker_first = true`: the Worker handles every request first and fetches files from Assets as needed

## Content

| Path | Contents |
| --- | --- |
| `articles/YYYY/MM/DD/index.md` | Articles with YAML frontmatter |
| `templates/` | `article.html`, `index.html`, `404.html`, `articles.md` |
| `assets/` | CSS, JS, icons (no template placeholders, copied verbatim) |
| `output/` | Fully generated, not committed |

Frontmatter format:

```yaml
---
title: "Article Title"
date: 2024-01-01T12:00:00+09:00
draft: false
tags:
  - tag1
  - tag2
---
```

## Deployment

- `output/` is deployed as Cloudflare Worker Assets (configured in `cloudflare.config.ts` / `wrangler.config.ts` for the `cf` CLI)
- GitHub Actions (`.github/workflows/deploy.yaml`) runs on push to `develop`: generate → build-worker → cf deploy → cf cache purge
