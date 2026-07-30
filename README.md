# mataku.today

Personal diary built with Kotlin.

## Modules

### Generator (Kotlin/JVM)

Converts Markdown files with YAML frontmatter to HTML. Uses `org.commonmark:commonmark` for GFM parsing.

Supports embeds: X/Twitter, Gist, YouTube and Spotify.

### Worker (Kotlin/JS)

Cloudflare Worker implemented in Kotlin/JS. Serves content from Worker Assets.

## Build

```shell
make new              # Create a new article (articles/YYYY/MM/DD/index.
make generate         # Generate HTML from markdown articles
make build-worker     # Build Cloudflare Worker
```

## Deployment

```shell
make deploy
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
assets/                     # Static files copied to output/assets/
├── styles.css
├── theme.js
├── favicon.ico
└── prism-*.min.js
output/                     # Generated output (not committed)
```
