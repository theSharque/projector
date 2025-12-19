import { test, expect } from '@playwright/test';

test.describe('Login', () => {
  test('should display login page', async ({ page }) => {
    await page.goto('/login');

    await expect(page.getByRole('heading', { name: /Projector Login/i })).toBeVisible();
    await expect(page.getByLabel(/email/i)).toBeVisible();
    await expect(page.getByLabel(/password/i)).toBeVisible();
    await expect(page.getByRole('button', { name: /login/i })).toBeVisible();
  });

  test('should show validation errors for empty form', async ({ page }) => {
    await page.goto('/login');

    await page.getByRole('button', { name: /login/i }).click();

    // Ant Design validation messages
    await expect(page.locator('.ant-form-item-explain-error').first()).toBeVisible();
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel(/email/i).fill('invalid@example.com');
    await page.getByLabel(/password/i).fill('wrongpassword');
    await page.getByRole('button', { name: /login/i }).click();

    // Wait for error toast (react-hot-toast)
    await expect(page.locator('[data-hot-toast]').or(page.locator('.go3958317564'))).toBeVisible({ timeout: 5000 });
  });

  test('should successfully login with valid credentials', async ({ page, context }) => {
    // Listen for console messages
    const consoleMessages: string[] = [];
    page.on('console', msg => {
      const text = msg.text();
      consoleMessages.push(`${msg.type()}: ${text}`);
      if (msg.type() === 'error') {
        console.log('Console error:', text);
      }
    });

    // Listen for network requests to backend API
    const apiRequests: string[] = [];
    page.on('request', request => {
      const url = request.url();
      if (url.includes('localhost:8080') || url.includes('/api/auth')) {
        apiRequests.push(`${request.method()} ${url}`);
        console.log('API Request:', `${request.method()} ${url}`);
      }
    });

    page.on('response', response => {
      const url = response.url();
      if (url.includes('localhost:8080') || url.includes('/api/auth')) {
        console.log(`API Response: ${response.status()} ${url}`);
        if (url.includes('/api/auth/login')) {
          const headers = response.headers();
          console.log('Set-Cookie header:', headers['set-cookie']);
        }
      }
    });

    await page.goto('/login');

    // Wait for page to load
    await expect(page.getByLabel(/email/i)).toBeVisible();

    // Use default admin credentials from backend (email: admin, password: admin)
    await page.getByLabel(/email/i).fill('admin');
    await page.getByLabel(/password/i).fill('admin');

    // Wait for login API call
    const loginResponsePromise = page.waitForResponse(
      response => response.url().includes('/api/auth/login'),
      { timeout: 10000 }
    ).catch(() => null);

    // Click login button
    await page.getByRole('button', { name: /login/i }).click();

    // Wait for login response
    const loginResponse = await loginResponsePromise;
    if (loginResponse) {
      console.log('Login response status:', loginResponse.status());
      const headers = loginResponse.headers();
      console.log('Set-Cookie:', headers['set-cookie']);
    } else {
      console.log('No login response received');
      console.log('Console messages:', consoleMessages);
      console.log('API requests:', apiRequests);
    }

    // Wait a bit more for navigation
    await page.waitForTimeout(2000);

    // Check cookies
    const cookies = await context.cookies();
    const authCookie = cookies.find(c => c.name === 'X-Auth');
    console.log('Auth cookie found:', !!authCookie);

    // Check current URL
    const currentUrl = page.url();
    console.log('Current URL:', currentUrl);

    // Should redirect to users page after successful login
    await expect(page).toHaveURL(/\/users/, { timeout: 20000 });

    // Check that cookie was set
    expect(authCookie).toBeDefined();
    expect(authCookie?.value).toBeTruthy();

    // Should see the main layout
    await expect(page.getByText('Projector')).toBeVisible();
    await expect(page.getByText(/users/i).first()).toBeVisible();
  });

  test('should redirect to login when accessing protected route without auth', async ({ page }) => {
    await page.goto('/users');

    // Should redirect to login
    await expect(page).toHaveURL(/\/login/, { timeout: 5000 });
  });

  test('should maintain session after page reload', async ({ page, context }) => {
    // Login first
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('admin');
    await page.getByLabel(/password/i).fill('admin');

    // Wait for navigation after login
    await Promise.all([
      page.waitForURL(/\/users/, { timeout: 15000 }),
      page.getByRole('button', { name: /login/i }).click(),
    ]);

    // Wait for redirect
    await expect(page).toHaveURL(/\/users/, { timeout: 15000 });

    // Check that cookie is set
    const cookies = await context.cookies();
    const authCookie = cookies.find(c => c.name === 'X-Auth');
    expect(authCookie).toBeDefined();

    // Reload page
    await page.reload();

    // Should still be authenticated (cookie persists)
    await expect(page).toHaveURL(/\/users/, { timeout: 10000 });
    await expect(page.getByText('Projector')).toBeVisible();
  });
});

