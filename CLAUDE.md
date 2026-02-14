# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

mataku.today is a personal blog built with Kotlin. It consists of a static site generator (JVM) and a Cloudflare Worker (Kotlin/JS) that serves static assets via Worker Assets binding.

## Build Commands

```shell
# Generate static HTML from markdown articles
make generate

# Build Cloudflare Worker
make build-worker

# Deploy (generate + build-worker + wrangler deploy)
make deploy

# Create a new article (creates articles/YYYY/MM/DD/index.md)
./gradlew :generator:new

# Generate RSS feed (feed.xml)
./gradlew :generator:feed
```

## Architecture

### Modules

- **generator** (`:generator`): JVM application that converts Markdown articles to HTML
  - Entry point: `blog.MainKt`
  - Uses Commonmark for Markdown parsing with GFM extensions (tables, strikethrough, autolink)
  - Transformers for embeds: X/Twitter, Gist, YouTube, Spotify, ImageCaption
  - Outputs to `output/` directory

- **worker** (`:worker`): Kotlin/JS Cloudflare Worker
  - Entry point: `worker/entry.js` imports compiled Kotlin/JS
  - Routes requests and serves content from Worker Assets (`env.ASSETS` binding)
  - Handles: articles, assets, index, pagination, robots.txt, sitemap.xml, feed.xml, privacy_policy
  - `run_worker_first = true`: Worker processes all requests first, fetching files from Assets as needed

### Deployment

- `output/` directory is deployed as Cloudflare Worker Assets (configured in `wrangler.toml`)
- GitHub Actions (`.github/workflows/deploy.yaml`) runs on push to `develop`: generate → build-worker → wrangler deploy → cache purge

### Content Structure

- Articles: `articles/YYYY/MM/DD/index.md` with YAML frontmatter (title, date, tags)
- Templates: `templates/` (article.html, index.html, 404.html, articles.md)
- Output: `output/` (generated HTML, not committed)

### Frontmatter Format

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
