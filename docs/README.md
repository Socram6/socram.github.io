# CAD Archive - GitHub Pages

This directory contains the static website files for GitHub Pages.

**Website URL:** https://socram6.github.io/socram.github.io/

## Structure

```
docs/
├── index.html          # Main page
├── css/
│   └── global.css      # Stylesheet
└── README.md           # This file
```

## Configuration

GitHub Pages is configured to serve from the `docs/` folder on the `main` branch.

### To update:
1. Modify files in the `docs/` directory
2. Commit and push to `main`
3. Changes will automatically deploy to the GitHub Pages site

## Backend Integration

Update the form action in `index.html` with your actual backend URL:

```html
<form action="https://your-backend-app-name.onrender.com/upload" method="post">
```
