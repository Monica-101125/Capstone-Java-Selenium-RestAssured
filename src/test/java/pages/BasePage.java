package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// Parent of every page class. Holds the driver and the reusable "wait then act" helpers.
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        // Explicit wait: waits up to 15 seconds, but only as long as the condition needs
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Wait until the element is visible on screen, then return it
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Wait until clickable, then click. If something covers it, fall back to a JavaScript click
    protected void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Click intercepted, using JavaScript click for: " + locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    // Wait for the field, empty it like a person would (Ctrl+A, Backspace), then type.
    // Selenium's clear() alone often fails on React-based search boxes, so old text stays.
    protected void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.click();
        element.clear();
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        element.sendKeys(text);
    }

    // Read the text typed inside an input field
    protected String getValue(By locator) {
        return waitForVisible(locator).getAttribute("value");
    }
}