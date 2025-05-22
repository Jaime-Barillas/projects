# Installation

1. Create a folder next to your `book.toml` called `mdbook-theme`.
2. Copy [mdbook-theme.css]() to the newly created folder.
3. Copy [mdbook-theme.js]() to the newly created folder.
4. Add the following to `book.toml`:
```toml
{{#include ../book.toml:output-html-header}}
{{#include ../book.toml:output-html-required}}
```

Consider adding the following configs:
```toml
{{#include ../book.toml:output-html-header}}
{{#include ../book.toml:output-html-recommended}}

{{#include ../book.toml:recommended-config-changes}}
```
