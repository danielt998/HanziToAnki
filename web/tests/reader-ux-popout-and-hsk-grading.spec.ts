import { expect, type Page, test } from '@playwright/test';

const DESKTOP_VIEWPORT = { width: 1280, height: 832 };
const MOBILE_VIEWPORT = { width: 393, height: 873 };

const VIEWPORTS = [
  { name: 'Desktop', size: DESKTOP_VIEWPORT },
  { name: 'Mobile', size: MOBILE_VIEWPORT },
] as const;

async function openClipReader(page: Page, viewport: { width: number; height: number }) {
  await page.setViewportSize(viewport);
  await page.goto('/');
  await expect(page.getByText('Text to Anki Deck')).toBeVisible({ timeout: 10000 });
  const readerBtnName = viewport.width >= 768 ? 'Clip Reader' : 'Reader';
  await page.getByRole('button', { name: readerBtnName, exact: true }).click();
  await expect(page.getByText('Interactive Clip Reader')).toBeVisible();
}

test.describe('Clip Reader UX: Segmentation Switcher, Typography, HSK Grading & Mobile Popout', () => {
  for (const vp of VIEWPORTS) {
    test(`${vp.name}: segmentation engine toggle and natural reader typography`, async ({
      page,
    }) => {
      await openClipReader(page, vp.size);

      // Segmentation Engine segmented control
      const engineGroup = page.getByRole('group', { name: 'Segmentation Engine' });
      await expect(engineGroup).toBeVisible();

      const jiebaBtn = engineGroup.getByRole('button', { name: 'Jieba' });
      const intlBtn = engineGroup.getByRole('button', { name: 'Browser Intl' });

      await expect(jiebaBtn).toBeVisible();
      await expect(intlBtn).toBeVisible();
      await expect(jiebaBtn).toHaveAttribute('aria-pressed', 'true');
      await expect(intlBtn).toHaveAttribute('aria-pressed', 'false');

      // Switch to Browser Intl
      await intlBtn.click();
      await expect(intlBtn).toHaveAttribute('aria-pressed', 'true');
      await expect(jiebaBtn).toHaveAttribute('aria-pressed', 'false');

      // Switch back to Jieba
      await jiebaBtn.click();
      await expect(jiebaBtn).toHaveAttribute('aria-pressed', 'true');
      await expect(intlBtn).toHaveAttribute('aria-pressed', 'false');

      // Verify natural prose typography (inline flow + preserved newlines, no tracking-wide)
      const readerContainer = page.locator('.font-hanzi.whitespace-pre-wrap');
      await expect(readerContainer).toBeVisible();
      await expect(readerContainer).not.toHaveClass(/tracking-wide/);
      const containerStyles = await readerContainer.evaluate((el) => {
        const computed = window.getComputedStyle(el);
        return { whiteSpace: computed.whiteSpace };
      });
      expect(containerStyles.whiteSpace).toBe('pre-wrap');

      const wordToken = readerContainer
        .locator('span')
        .filter({ hasText: /^故事$/ })
        .first();
      await expect(wordToken).toBeVisible();
      await expect(wordToken).not.toHaveClass(/inline-block/);
      await expect(wordToken).not.toHaveClass(/mx-0\.5/);
      const tokenDisplay = await wordToken.evaluate((el) => window.getComputedStyle(el).display);
      expect(tokenDisplay).toBe('inline');

      if (vp.size.width <= 400) {
        const scrollWidth = await page.evaluate(() => document.documentElement.scrollWidth);
        expect(scrollWidth).toBeLessThanOrEqual(vp.size.width);
      }
    });

    test(`${vp.name}: HSK Grading toggles tier checkboxes and highlights matching words`, async ({
      page,
    }) => {
      await openClipReader(page, vp.size);

      const gradingToggle = page.getByRole('button', { name: 'HSK Grading' });
      await expect(gradingToggle).toBeVisible();
      await expect(gradingToggle).toHaveAttribute('aria-pressed', 'false');

      // Filter checkboxes should be hidden initially
      await expect(page.getByRole('checkbox', { name: 'HSK 1' })).toHaveCount(0);

      // Enable HSK Grading
      await gradingToggle.click();
      await expect(gradingToggle).toHaveAttribute('aria-pressed', 'true');

      for (const level of [1, 2, 3, 4, 5, 6]) {
        const checkbox = page.getByRole('checkbox', { name: `HSK ${level}` });
        await expect(checkbox).toBeVisible();
        await expect(checkbox).toBeChecked();
      }
      const untieredCheckbox = page.getByRole('checkbox', { name: 'Untiered' });
      await expect(untieredCheckbox).toBeVisible();
      await expect(untieredCheckbox).not.toBeChecked();

      const readerContainer = page.locator('.font-hanzi.whitespace-pre-wrap');
      const hsk1Token = readerContainer
        .locator('span')
        .filter({ hasText: /^中国$/ })
        .first();
      const hsk2Token = readerContainer
        .locator('span')
        .filter({ hasText: /^妻子$/ })
        .first();
      const hsk3Token = readerContainer
        .locator('span')
        .filter({ hasText: /^故事$/ })
        .first();
      const hsk4Token = readerContainer
        .locator('span')
        .filter({ hasText: /^许多$/ })
        .first();
      const hsk5Token = readerContainer
        .locator('span')
        .filter({ hasText: /^古代$/ })
        .first();
      const hsk6Token = readerContainer
        .locator('span')
        .filter({ hasText: /^寓言$/ })
        .first();

      await expect(hsk1Token).toHaveClass(/(?:^|\s)bg-emerald-500\/15(?:\s|$)/);
      await expect(hsk2Token).toHaveClass(/(?:^|\s)bg-sky-500\/15(?:\s|$)/);
      await expect(hsk3Token).toHaveClass(/(?:^|\s)bg-amber-400\/20(?:\s|$)/);
      await expect(hsk4Token).toHaveClass(/(?:^|\s)bg-orange-500\/15(?:\s|$)/);
      await expect(hsk5Token).toHaveClass(/(?:^|\s)bg-rose-500\/15(?:\s|$)/);
      await expect(hsk6Token).toHaveClass(/(?:^|\s)bg-purple-500\/15(?:\s|$)/);

      // Uncheck HSK 3: removes HSK 3 highlight while keeping HSK 1 highlighted
      await page.getByRole('checkbox', { name: 'HSK 3' }).uncheck();
      await expect(hsk3Token).not.toHaveClass(/(?:^|\s)bg-amber-400\/20(?:\s|$)/);
      await expect(hsk1Token).toHaveClass(/(?:^|\s)bg-emerald-500\/15(?:\s|$)/);

      // Wait for background full CC-CEDICT load so Untiered words (e.g. 农夫) become clickable
      const untieredToken = readerContainer
        .locator('span')
        .filter({ hasText: /^(农夫|农)$/ })
        .first();
      await expect(untieredToken).toHaveClass(/cursor-pointer/, { timeout: 15000 });
      await expect(untieredToken).not.toHaveClass(/(?:^|\s)bg-slate-500\/15(?:\s|$)/);

      // Check Untiered: applies slate highlight
      await untieredCheckbox.check();
      await expect(untieredToken).toHaveClass(/(?:^|\s)bg-slate-500\/15(?:\s|$)/);

      // Quick actions: None / All
      await page.getByRole('button', { name: 'None', exact: true }).click();
      await expect(hsk1Token).not.toHaveClass(/(?:^|\s)bg-emerald-500\/15(?:\s|$)/);
      await expect(untieredToken).not.toHaveClass(/(?:^|\s)bg-slate-500\/15(?:\s|$)/);

      await page.getByRole('button', { name: 'All', exact: true }).click();
      await expect(hsk1Token).toHaveClass(/(?:^|\s)bg-emerald-500\/15(?:\s|$)/);
      await expect(hsk3Token).toHaveClass(/(?:^|\s)bg-amber-400\/20(?:\s|$)/);
      await expect(untieredToken).toHaveClass(/(?:^|\s)bg-slate-500\/15(?:\s|$)/);

      // Disable HSK Grading
      await gradingToggle.click();
      await expect(gradingToggle).toHaveAttribute('aria-pressed', 'false');
      await expect(page.getByRole('checkbox', { name: 'HSK 1' })).toHaveCount(0);
      await expect(hsk1Token).not.toHaveClass(/(?:^|\s)bg-emerald-500\/15(?:\s|$)/);
    });
  }

  test('Mobile (393x873): tapping a word opens Word Definition popout within viewport and Close dismisses it', async ({
    page,
  }) => {
    await openClipReader(page, MOBILE_VIEWPORT);

    // No static Word Definition card rendered at the bottom on mobile
    await expect(page.getByText('Word Definition')).toHaveCount(0);
    await expect(page.locator('[role="dialog"]')).toHaveCount(0);

    // Tap "故事"
    const readerContainer = page.locator('.font-hanzi.whitespace-pre-wrap');
    const storyToken = readerContainer
      .locator('span')
      .filter({ hasText: /^故事$/ })
      .first();
    await storyToken.click();

    // Popout dialog opens with expected content and a single Word Definition heading
    const dialog = page.locator('[role="dialog"]');
    await expect(dialog).toBeVisible();
    await expect(page.getByText('Word Definition')).toHaveCount(1);
    await expect(dialog.getByText('Word Definition')).toBeVisible();
    await expect(dialog.getByText('gù shi')).toBeVisible();
    await expect(dialog.getByText('story')).toBeVisible();

    const addBtn = dialog.getByRole('button', { name: 'Add to Session Deck' });
    await expect(addBtn).toBeVisible();

    // Dialog container and inner card sit completely inside the 393x873 mobile viewport
    const dialogBox = await dialog.boundingBox();
    expect(dialogBox).not.toBeNull();
    expect(dialogBox!.y).toBeGreaterThanOrEqual(0);
    expect(dialogBox!.y + dialogBox!.height).toBeLessThanOrEqual(MOBILE_VIEWPORT.height);

    const cardBox = await dialog.locator(':scope > div').boundingBox();
    expect(cardBox).not.toBeNull();
    expect(cardBox!.y).toBeGreaterThanOrEqual(0);
    expect(cardBox!.y + cardBox!.height).toBeLessThanOrEqual(MOBILE_VIEWPORT.height);

    // Add to session deck from inside the popout
    await addBtn.click();
    await expect(dialog.getByRole('button', { name: 'Added to Session Deck' })).toBeVisible();
    await expect(page.getByText('1 saved')).toBeVisible();

    // Click Close (X) to dismiss popout
    await dialog.getByRole('button', { name: 'Close' }).click();
    await expect(dialog).toHaveCount(0);
    await expect(page.getByText('Word Definition')).toHaveCount(0);
  });

  test('Desktop (1280x832) to Mobile (393x873) resize keeps a single Word Definition inspector', async ({
    page,
  }) => {
    await openClipReader(page, DESKTOP_VIEWPORT);

    // Desktop sidebar shows Word Definition without a modal dialog
    await expect(page.getByText('Word Definition')).toHaveCount(1);
    await expect(page.getByText('No word selected')).toBeVisible();
    await expect(page.locator('[role="dialog"]')).toHaveCount(0);

    // Select a word on Desktop
    const readerContainer = page.locator('.font-hanzi.whitespace-pre-wrap');
    await readerContainer
      .locator('span')
      .filter({ hasText: /^故事$/ })
      .first()
      .click();
    await expect(page.getByText('gù shi')).toBeVisible();
    await expect(page.locator('[role="dialog"]')).toHaveCount(0);

    // Resize to Mobile: desktop sidebar unmounts and mobile popout mounts with single instance
    await page.setViewportSize(MOBILE_VIEWPORT);
    const dialog = page.locator('[role="dialog"]');
    await expect(dialog).toBeVisible();
    await expect(page.getByText('Word Definition')).toHaveCount(1);
    await expect(dialog.getByText('gù shi')).toBeVisible();
  });
});
