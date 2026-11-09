import fs from 'node:fs';
import path from 'node:path';
import { expect, test } from '@playwright/test';
import { generateTestApkg } from './fixtures/create_fixture';

// Real-world target device viewport dimensions:
// Desktop: Apple MacBook Air 13" (1280 x 832)
// Mobile: Google Pixel 8 (393 x 873)
const MACBOOK_AIR_VIEWPORT = { width: 1280, height: 832 };
const PIXEL_8_VIEWPORT = { width: 393, height: 873 };

test.describe('HanziToAnki Web End-to-End User Stories', () => {
  test.beforeAll(async () => {
    fs.mkdirSync('docs/screenshots', { recursive: true });
  });

  test('Story 1: Text-to-Deck vocabulary extraction and Anki TSV preview', async ({
    page,
  }) => {
    await page.setViewportSize(MACBOOK_AIR_VIEWPORT);
    await page.goto('/');

    // Verify main header and wait for dictionary readiness
    await expect(page.getByText('Text to Anki Deck')).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByText('Bao the Scholar says:')).toBeVisible();

    // Select the "成语 Idioms" sample button
    await page.getByRole('button', { name: '成语 Idioms' }).click();

    // Verify segmentation extracts 4-character idioms
    await expect(page.locator('tbody').getByText('一心一意')).toBeVisible();
    await expect(page.locator('tbody').getByText('画蛇添足')).toBeVisible();

    // Verify Anki export button is active
    const exportBtn = page.getByRole('button', { name: 'Export Anki Deck' });
    await expect(exportBtn).toBeEnabled();

    // Capture Tab 1 Desktop Screenshot (MacBook Air)
    await page.screenshot({
      path: 'docs/screenshots/tab1-text-to-deck-desktop.png',
    });
    await page.screenshot({ path: 'docs/screenshots/tab1-text-to-deck.png' }); // legacy fallback

    // Capture Tab 1 Mobile Screenshot (Google Pixel 8)
    await page.setViewportSize(PIXEL_8_VIEWPORT);
    await page.screenshot({
      path: 'docs/screenshots/tab1-text-to-deck-mobile.png',
    });
  });

  test('Story 2: Flashcard Tuner filters deck by exact HSK tiers', async ({
    page,
  }) => {
    await page.setViewportSize(MACBOOK_AIR_VIEWPORT);
    await page.goto('/');

    // Switch to Tuner tab
    await page.getByRole('button', { name: 'Flashcard Tuner' }).click();
    await expect(
      page.getByRole('heading', { name: 'Flashcard Tuner' }),
    ).toBeVisible();

    // By default, only HSK 4 is enabled. Verify HSK 4 cards are isolated
    await expect(page.getByText('Cards Kept')).toBeVisible();
    await expect(
      page.getByRole('cell', { name: '安全', exact: true }),
    ).toBeVisible();

    // Toggle HSK 5 on
    await page.getByText('HSK 5', { exact: true }).click();
    await expect(
      page.getByRole('cell', { name: '爱惜', exact: true }),
    ).toBeVisible(); // HSK 5

    // Capture Tab 2 Desktop Screenshot (MacBook Air)
    await page.screenshot({
      path: 'docs/screenshots/tab2-flashcard-tuner-desktop.png',
    });
    await page.screenshot({
      path: 'docs/screenshots/tab2-flashcard-tuner.png',
    }); // legacy fallback

    // Capture Tab 2 Mobile Screenshot (Google Pixel 8)
    await page.setViewportSize(PIXEL_8_VIEWPORT);
    await page.screenshot({
      path: 'docs/screenshots/tab2-flashcard-tuner-mobile.png',
    });
  });

  test('Story 3: Interactive Clip Reader tokenization and session deck', async ({
    page,
  }) => {
    await page.setViewportSize(MACBOOK_AIR_VIEWPORT);
    await page.goto('/');

    // Switch to Reader tab
    await page.getByRole('button', { name: 'Clip Reader' }).click();
    await expect(page.getByText('Interactive Clip Reader')).toBeVisible();

    // Click on the Chinese word "故事" (story) inside the reading pane
    const wordToken = page
      .locator('span')
      .filter({ hasText: /^故事$/ })
      .first();
    await expect(wordToken).toBeVisible();
    await wordToken.click();

    // Verify the live inspector displays the word, pinyin, and definitions
    await expect(page.getByText('Word Definition')).toBeVisible();
    await expect(page.getByText('gù shi')).toBeVisible();
    await expect(page.getByText('story')).toBeVisible();

    // Click "Add to Session Deck"
    const addBtn = page.getByRole('button', { name: 'Add to Session Deck' });
    await addBtn.click();
    await expect(page.getByText('Added to Session Deck')).toBeVisible();
    await expect(page.getByText('1 saved')).toBeVisible();

    // Capture Tab 3 Desktop Screenshot (MacBook Air)
    await page.screenshot({
      path: 'docs/screenshots/tab3-clip-reader-desktop.png',
    });
    await page.screenshot({ path: 'docs/screenshots/tab3-clip-reader.png' }); // legacy fallback

    // Capture Tab 3 Mobile Screenshot (Google Pixel 8)
    await page.setViewportSize(PIXEL_8_VIEWPORT);
    await page.screenshot({
      path: 'docs/screenshots/tab3-clip-reader-mobile.png',
    });
  });

  test('Story 4: Global Settings Modal and Tone Palettes', async ({ page }) => {
    await page.setViewportSize(MACBOOK_AIR_VIEWPORT);
    await page.goto('/');

    // Open settings modal
    await page.getByTitle('Settings & Tone Palette').click();
    await expect(page.getByText('Preferences & Tone Settings')).toBeVisible();
    await expect(page.getByText('Standard', { exact: true })).toBeVisible();
    await expect(page.getByText('Vibrant Rainbow')).toBeVisible();

    // Capture Settings Modal Desktop Screenshot (MacBook Air)
    await page.screenshot({
      path: 'docs/screenshots/settings-modal-desktop.png',
    });
    await page.screenshot({ path: 'docs/screenshots/settings-modal.png' }); // legacy fallback

    // Capture Settings Modal Mobile Screenshot (Google Pixel 8)
    await page.setViewportSize(PIXEL_8_VIEWPORT);
    await page.screenshot({
      path: 'docs/screenshots/settings-modal-mobile.png',
    });
  });

  test('Story 5: Tuner cleans up .apkg deck with embedded audio and exports filtered .apkg', async ({
    page,
  }) => {
    const fixturePath = path.resolve('tests/fixtures/minimal_hsk1_hsk2.apkg');
    if (!fs.existsSync(fixturePath)) {
      await generateTestApkg();
    }

    await page.setViewportSize(MACBOOK_AIR_VIEWPORT);
    await page.goto('/');

    // Switch to Tuner tab
    await page.getByRole('button', { name: 'Flashcard Tuner' }).click();
    await expect(
      page.getByRole('heading', { name: 'Flashcard Tuner' }),
    ).toBeVisible();

    // Upload minimal .apkg with HSK 1 (你好) and HSK 2 (帮助)
    await page.locator('input[type="file"]').setInputFiles(fixturePath);

    // Verify .apkg audio status banner appears
    await expect(
      page.getByText('Native .apkg package loaded (2 audio tracks)'),
    ).toBeVisible({ timeout: 10000 });

    // Untoggle default HSK 4, and toggle HSK 1 only
    await page.getByText('HSK 4', { exact: true }).click(); // unselect HSK 4
    await page.getByText('HSK 1', { exact: true }).click(); // select HSK 1

    // Verify cleanup: only HSK 1 word (朋友) is kept, HSK 2 word (帮助) is filtered out
    await expect(page.getByText('Kept Flashcards (1)')).toBeVisible();
    await expect(
      page.getByRole('cell', { name: '朋友', exact: true }),
    ).toBeVisible();
    await expect(
      page.getByRole('cell', { name: '帮助', exact: true }),
    ).not.toBeVisible();

    // Verify both Export .apkg and Export TSV are available
    const exportApkgBtn = page.getByRole('button', {
      name: 'Export .apkg (Audio Kept)',
    });
    await expect(exportApkgBtn).toBeVisible();
    await expect(exportApkgBtn).toBeEnabled();

    // Trigger download and verify exported filename contains _tuned_hanziToAnki.apkg
    const downloadPromise = page.waitForEvent('download');
    await exportApkgBtn.click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe(
      'minimal_hsk1_hsk2_tuned_hanziToAnki.apkg',
    );
  });
});
