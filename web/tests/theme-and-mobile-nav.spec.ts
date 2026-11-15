import { expect, type Locator, type Page, test } from '@playwright/test';

// Real-world target device viewport dimensions:
// Desktop: Apple MacBook Air 13" (1280 x 832)
// Mobile: Google Pixel 8 (393 x 873)
const MACBOOK_AIR_VIEWPORT = { width: 1280, height: 832 };
const PIXEL_8_VIEWPORT = { width: 393, height: 873 };

const LIGHT_BG = 'rgb(251, 249, 245)';
const DARK_BG = 'rgb(24, 26, 29)';
const THEME_STORAGE_KEY = 'hanzitoanki-theme';

async function expectElementUnoccluded(locator: Locator): Promise<void> {
  const isTopTarget = await locator.evaluate((el) => {
    const rect = el.getBoundingClientRect();
    const topEl = document.elementFromPoint(rect.left + rect.width / 2, rect.top + rect.height / 2);
    return topEl === el || el.contains(topEl);
  });
  expect(isTopTarget).toBe(true);
}

async function assertThemeState(page: Page, isDark: boolean): Promise<void> {
  const html = page.locator('html');
  const rootDiv = page.locator('#root > div').first();
  const themeToggle = page.getByTitle(/Switch to (Light|Dark) Mode/);

  if (isDark) {
    await expect(html).toHaveClass(/\bdark\b/);
    await expect(rootDiv).toHaveCSS('background-color', DARK_BG);
    await expect(themeToggle).toHaveAttribute('title', 'Switch to Light Mode');
    await expect(themeToggle).toHaveAttribute('aria-label', 'Switch to Light Mode');
  } else {
    await expect(html).not.toHaveClass(/\bdark\b/);
    await expect(rootDiv).toHaveCSS('background-color', LIGHT_BG);
    await expect(themeToggle).toHaveAttribute('title', 'Switch to Dark Mode');
    await expect(themeToggle).toHaveAttribute('aria-label', 'Switch to Dark Mode');
  }

  const storedTheme = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    THEME_STORAGE_KEY,
  );
  expect(storedTheme).toBe(isDark ? 'dark' : 'light');
}

async function verifyToggleAndPersistence(page: Page, initialDark: boolean): Promise<void> {
  const themeToggle = page.getByTitle(/Switch to (Light|Dark) Mode/);

  await assertThemeState(page, initialDark);

  // Toggle to opposite theme and verify DOM + localStorage + computed styles update
  await themeToggle.click();
  await assertThemeState(page, !initialDark);

  // Reload and confirm toggled preference persists
  await page.reload();
  await assertThemeState(page, !initialDark);

  // Toggle back to original theme and confirm persistence across reload
  await page.getByTitle(/Switch to (Light|Dark) Mode/).click();
  await assertThemeState(page, initialDark);

  await page.reload();
  await assertThemeState(page, initialDark);
}

test.describe('Theme Persistence & Mobile Navigation Layout', () => {
  test('Desktop (1280x832): light/dark theme toggle updates styles and persists across reloads', async ({
    page,
  }) => {
    await page.emulateMedia({ colorScheme: 'light' });
    await page.setViewportSize(MACBOOK_AIR_VIEWPORT);
    await page.goto('/');

    const mobileDock = page.getByRole('navigation', { name: 'Mobile Navigation' });
    await expect(mobileDock).toBeHidden();

    const desktopNav = page.getByRole('navigation', { name: 'Primary' });
    await expect(desktopNav).toBeVisible();

    await verifyToggleAndPersistence(page, false);
  });

  test('Mobile (393x873): header and bottom dock do not overlap and header controls remain accessible', async ({
    page,
  }) => {
    await page.emulateMedia({ colorScheme: 'light' });
    await page.setViewportSize(PIXEL_8_VIEWPORT);
    await page.goto('/');

    const header = page.locator('header');
    const mobileDock = page.getByRole('navigation', { name: 'Mobile Navigation' });
    const themeToggle = page.getByTitle(/Switch to (Light|Dark) Mode/);
    const settingsButton = page.getByTitle('Settings & Tone Palette');

    await expect(header).toBeVisible();
    await expect(mobileDock).toBeVisible();
    await expect(themeToggle).toBeVisible();
    await expect(settingsButton).toBeVisible();

    // Ensure no horizontal overflow on Pixel 8
    const scrollWidth = await page.evaluate(() => document.documentElement.scrollWidth);
    expect(scrollWidth).toBeLessThanOrEqual(PIXEL_8_VIEWPORT.width);

    const headerBox = await header.boundingBox();
    const dockBox = await mobileDock.boundingBox();
    const themeBox = await themeToggle.boundingBox();
    const settingsBox = await settingsButton.boundingBox();

    expect(headerBox).not.toBeNull();
    expect(dockBox).not.toBeNull();
    expect(themeBox).not.toBeNull();
    expect(settingsBox).not.toBeNull();

    // Header stays anchored at top (y = 0) with ~64px height
    expect(headerBox!.y).toBe(0);
    expect(headerBox!.height).toBeGreaterThanOrEqual(63);
    expect(headerBox!.height).toBeLessThanOrEqual(66);

    // Mobile bottom dock sits near the bottom of the 873px viewport, well below header
    expect(dockBox!.y).toBeGreaterThan(750);
    expect(dockBox!.y + dockBox!.height).toBeLessThanOrEqual(PIXEL_8_VIEWPORT.height + 1);
    expect(dockBox!.y).toBeGreaterThan(headerBox!.y + headerBox!.height);

    // Theme toggle and Settings buttons are touch-friendly (>= 40px) and fit inside header
    expect(themeBox!.height).toBeGreaterThanOrEqual(40);
    expect(settingsBox!.height).toBeGreaterThanOrEqual(40);
    expect(themeBox!.y).toBeGreaterThanOrEqual(0);
    expect(themeBox!.y + themeBox!.height).toBeLessThanOrEqual(headerBox!.y + headerBox!.height);
    expect(settingsBox!.y).toBeGreaterThanOrEqual(0);
    expect(settingsBox!.y + settingsBox!.height).toBeLessThanOrEqual(
      headerBox!.y + headerBox!.height,
    );
    expect(themeBox!.x + themeBox!.width).toBeLessThanOrEqual(settingsBox!.x);
    expect(settingsBox!.x + settingsBox!.width).toBeLessThanOrEqual(PIXEL_8_VIEWPORT.width);

    // Verify neither button is occluded by the mobile dock or another overlay
    await expectElementUnoccluded(themeToggle);
    await expectElementUnoccluded(settingsButton);

    // Mobile bottom dock tabs remain clickable and switch views
    await expect(page.getByText('Text to Anki Deck')).toBeVisible({ timeout: 10000 });
    await mobileDock.getByRole('button', { name: 'Tuner' }).click();
    await expect(page.getByRole('heading', { name: 'Flashcard Tuner' })).toBeVisible();

    await mobileDock.getByRole('button', { name: 'Reader' }).click();
    await expect(page.getByText('Interactive Clip Reader')).toBeVisible();

    await mobileDock.getByRole('button', { name: 'Creator' }).click();
    await expect(page.getByText('Text to Anki Deck')).toBeVisible();
  });

  test('Mobile (393x873): light/dark theme toggle updates styles and persists across reloads', async ({
    page,
  }) => {
    await page.emulateMedia({ colorScheme: 'light' });
    await page.setViewportSize(PIXEL_8_VIEWPORT);
    await page.goto('/');

    await verifyToggleAndPersistence(page, false);
  });

  test('Defaults to system prefers-color-scheme: dark when no preference is stored and persists manual override', async ({
    page,
  }) => {
    await page.emulateMedia({ colorScheme: 'dark' });
    await page.setViewportSize(MACBOOK_AIR_VIEWPORT);
    await page.goto('/');

    await verifyToggleAndPersistence(page, true);
  });
});
