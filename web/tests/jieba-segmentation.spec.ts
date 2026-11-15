import { expect, test } from '@playwright/test';

test.describe('Statistical Jieba Segmentation and Dictionary Polyphone Resolution', () => {
  test('Clip Reader uses Jieba segmentation and resolves polyphones cleanly', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('Text to Anki Deck')).toBeVisible({ timeout: 10000 });

    // Switch to Clip Reader
    await page.getByRole('button', { name: 'Clip Reader' }).click();
    await expect(page.getByText('Interactive Clip Reader')).toBeVisible();

    // "发展" in "必须遵循事物的发展规律" must be a single clickable span, not split into "发" + "展"
    const fazhanToken = page
      .locator('span')
      .filter({ hasText: /^发展$/ })
      .first();
    await expect(fazhanToken).toBeVisible();
    await expect(page.locator('span').filter({ hasText: /^发$/ })).toHaveCount(0);
    await expect(page.locator('span').filter({ hasText: /^展$/ })).toHaveCount(0);

    await fazhanToken.click();
    await expect(page.getByText('Word Definition')).toBeVisible();
    await expect(page.getByText('fā zhǎn', { exact: true })).toBeVisible();
    await expect(
      page.getByText('development; growth; to develop; to grow; to expand'),
    ).toBeVisible();
    await expect(page.getByText('fà', { exact: true })).toHaveCount(0);

    // "一个" in "其中一个是“拔苗助长”" must be clickable and show "yī gè" / "one"
    const yigeToken = page
      .locator('span')
      .filter({ hasText: /^一个$/ })
      .first();
    await expect(yigeToken).toBeVisible();
    await yigeToken.click();
    await expect(page.getByText('yī gè', { exact: true })).toBeVisible();
    await expect(page.getByText('one; a; an; single; individual')).toBeVisible();

    // Single character "发" must display primary reading fā (to send out / to develop), not fà
    await page.getByText('Edit or Paste Custom Article Text').click();
    await page.getByPlaceholder('Paste custom Chinese text here...').fill('发');

    const faToken = page.locator('span').filter({ hasText: /^发$/ }).first();
    await expect(faToken).toBeVisible();
    await faToken.click();
    await expect(page.getByText('fā', { exact: true })).toBeVisible();
    await expect(page.getByText(/to send out/)).toBeVisible();
    await expect(page.getByText(/to develop/)).toBeVisible();
    await expect(page.getByText('fà', { exact: true })).toHaveCount(0);
  });

  test('TextToDeck switches between Jieba (Statistical) and Browser (Intl) engines', async ({
    page,
  }) => {
    await page.goto('/');
    await expect(page.getByText('Text to Anki Deck')).toBeVisible({ timeout: 10000 });

    const engineSelect = page.getByLabel('Segmentation Engine');
    await expect(engineSelect).toHaveValue('jieba');

    const textarea = page.getByPlaceholder('粘贴中文文本 / Paste Chinese text here...');
    await textarea.fill('必须遵循事物的发展规律。一心一意、画蛇添足。');

    const tbody = page.locator('tbody');
    await expect(tbody.getByText('发展', { exact: true })).toBeVisible();
    await expect(tbody.getByText('一心一意', { exact: true })).toBeVisible();
    await expect(tbody.getByText('画蛇添足', { exact: true })).toBeVisible();
    await expect(tbody.getByText('发', { exact: true })).toHaveCount(0);
    await expect(tbody.getByText('展', { exact: true })).toHaveCount(0);

    // Switch to Browser (Intl)
    await engineSelect.selectOption('intl');
    await expect(engineSelect).toHaveValue('intl');
    await expect(tbody.getByText('发展', { exact: true })).toBeVisible();
    await expect(tbody.getByText('一心一意', { exact: true })).toBeVisible();
    await expect(tbody.getByText('画蛇添足', { exact: true })).toBeVisible();

    // Switch back to Jieba (Statistical) and verify 成语 Idioms sample extraction
    await engineSelect.selectOption('jieba');
    await expect(engineSelect).toHaveValue('jieba');
    await page.getByRole('button', { name: '成语 Idioms' }).click();
    await expect(tbody.getByText('一心一意', { exact: true })).toBeVisible();
    await expect(tbody.getByText('画蛇添足', { exact: true })).toBeVisible();
    await expect(tbody.getByText('望梅止渴', { exact: true })).toBeVisible();
    await expect(tbody.getByText('守株待兔', { exact: true })).toBeVisible();
  });
});
