from playwright.sync_api import sync_playwright, expect
import re

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Listen for all console events and print them
    page.on("console", lambda msg: print(f"BROWSER CONSOLE: {msg.text}"))

    try:
        print("Navigating to http://localhost:3000/")
        page.goto("http://localhost:3000/")

        print("Clicking 'Log in' button")
        page.get_by_role("button", name="Log in").click()

        # Warten auf die Keycloak-Anmeldeseite und deren URL
        print("Waiting for Keycloak login page...")
        expect(page).to_have_url(re.compile(".*localhost:8080.*"), timeout=10000)
        print("Redirected to Keycloak successfully.")

        page.locator('input[name="username"]').fill("adminuser")
        page.locator('input[name="password"]').fill("admin")
        page.locator('input[name="login"]').click()

        # Warten auf die Weiterleitung zurück zur Anwendung
        print("Waiting for redirect back to the app...")
        expect(page).to_have_url("http://localhost:3000/", timeout=15000)
        print("Redirected back to app successfully.")

        print("Navigating to Admin page...")
        page.get_by_role("link", name="Admin").click()

        # Warten auf die Benutzertabelle
        print("Waiting for user table...")
        expect(page.locator("table")).to_be_visible()

        print("Taking screenshot...")
        page.screenshot(path="jules-scratch/verification/admin_page.png")
        print("Screenshot saved to jules-scratch/verification/admin_page.png")

    except Exception as e:
        print(f"An error occurred: {e}")
        page.screenshot(path="jules-scratch/verification/error.png")
        print("Error screenshot saved to jules-scratch/verification/error.png")

    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)