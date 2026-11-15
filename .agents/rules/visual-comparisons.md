# Visual Testing & PR Screenshot Policy

## 1. Visual Verification Rule
Any change that affects UI, styling, layout, typography, or component states MUST:
1. Run Playwright E2E tests (`cd web && npm run test:e2e`) to update automated screenshots in `web/docs/screenshots/`.
2. Include the dual-viewport comparison table in the PR description as defined in `.github/pull_request_template.md`.

## 2. Standard Viewports
- **Desktop Viewport**: Apple MacBook Air 13" (`1280 × 832`).
- **Mobile Viewport**: Google Pixel 8 (`393 × 873`).

## 3. Visual Comparison Format in PRs
For visual changes, the PR MUST present a markdown table:
```markdown
| Feature / View | Desktop View (MacBook Air 1280×832) | Mobile View (Pixel 8 393×873) |
| :--- | :--- | :--- |
| **Tab 1: Text-to-Deck** | <img src="..." width="480" /> | <img src="..." width="220" /> |
| **Tab 2: Flashcard Tuner** | <img src="..." width="480" /> | <img src="..." width="220" /> |
| **Tab 3: Clip Reader** | <img src="..." width="480" /> | <img src="..." width="220" /> |
```
For refactors or design tweaks of existing features, provide **Before vs After** screenshots.
