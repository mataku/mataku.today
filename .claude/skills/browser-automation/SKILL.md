---
name: browser-automation
description: Preview generated HTML and perform browser automation
allowed-tools:
  - Bash(agent-browser:*)
  - Read
---


## Browser Automation

Use `agent-browser` for web automation. Run `agent-browser --help` for all commands.

Basic usage:

```
agent-browser open <url>              # Navigate to URL
agent-browser click <sel>             # Click element
agent-browser scroll <dir> [px]       # Scroll (up/down/left/right)
agent-browser screenshot [path]       # Take screenshot (--full for full page, saves to a temporary directory if no path)
agent-browser snapshot                # Accessibility tree with refs (best for AI)
agent-browser close                   # Close browser (aliases: quit, exit)
```

## Use Cases
- Preview generated HTML at `http://localhost:8000`
- Verify page layout and styles