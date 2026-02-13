# mataku.today

A personal diary built with Kotlin static site generator and Cloudflare Worker.

## Architecture

- **generator** - JVM application that converts Markdown to HTML
  - Uses Commonmark for Markdown parsing with GFM extensions (tables, strikethrough, autolink)
  - Supports embeds: X/Twitter, Gist, YouTube, Spotify
- **worker** - Cloudflare Worker implemented in Kotlin/JS
  - Serves content from R2 bucket

## Development

```shell
# Generate HTML from markdown articles
make generate

# Build Cloudflare Worker
make build-worker

# Create a new article (articles/YYYY/MM/DD/index.md)
./gradlew :generator:new

# Generate RSS feed
./gradlew :generator:feed
```

## Content Structure

```
articles/
└── YYYY/
    └── MM/
        └── DD/
            └── index.md    # Frontmatter: title, date, tags
templates/
├── article.html
├── index.html
└── 404.html
output/                     # Generated HTML (not committed)
```
