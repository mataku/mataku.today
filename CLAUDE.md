# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

mataku.today is a personal blog built with Kotlin. It consists of a static site generator (JVM) and a Cloudflare Worker (Kotlin/JS) that serves content from R2 storage.

## Build Commands

```shell
# Generate static HTML from markdown articles
make generate

# Build Cloudflare Worker
make build-worker

# Create a new article (creates articles/YYYY/MM/DD/index.md)
./gradlew :generator:new

# Generate RSS feed (feed.xml)
./gradlew :generator:feed

# Upload generated files to R2
make upload-r2

# Deploy a single image to R2
make deploy-image path/to/image.png

# Local development server
make serve
```

## Architecture

### Modules

- **generator** (`:generator`): JVM application that converts Markdown articles to HTML
  - Entry point: `blog.MainKt`
  - Uses Commonmark for Markdown parsing with GFM extensions (tables, strikethrough, autolink)
  - Transformers for embeds: X/Twitter, Gist, YouTube, Spotify
  - Outputs to `output/` directory

- **worker** (`:worker`): Kotlin/JS Cloudflare Worker
  - Entry point: `worker.kt` (exports `fetch` function)
  - Routes requests and serves content from R2 bucket
  - Handles: articles, assets, index, pagination, robots.txt, sitemap.xml

### Content Structure

- Articles: `articles/YYYY/MM/DD/index.md` with YAML frontmatter (title, date, tags)
- Templates: `templates/` (article.html, index.html, 404.html, article.md)
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
