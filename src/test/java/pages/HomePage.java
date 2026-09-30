package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
// Represents the MakeMyTrip home page (scenario steps 2 to 4).
// NOTE: These locators are starting points. The site changes often, so verify each one
// in Chrome DevTools (F12) and fix it here if needed. Only this file needs changing.
public class HomePage extends BasePage {

    // ---- Locators (where things are on the page) ----
    private final By closePopupButton = By.xpath("//span[@data-cy='closeModal']");
    private final By roundTripTab     = By.xpath("//li[@data-cy='roundTrip']");
    private final By fromField        = By.id("fromCity");
    private final By toField          = By.id("toCity");
    private final By cityTextBox      = By.xpath("//input[@placeholder='Enter city or airport' or @placeholder='From' or @placeholder='To']");
    // Finds the suggestion whose text contains the city we typed (case-insensitive)
    private By suggestionFor(String city) {
        return By.xpath("(//ul[@role='listbox']//li[contains(translate(., "
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + city.toLowerCase() + "')])[1]");
    }

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // Step 2: close the sign up / sign in popup, if it appears
    public void closeLoginPopupIfPresent() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(closePopupButton))
                    .click();
            System.out.println("Popup closed");
        } catch (TimeoutException e) {
            // Close button not found. Many popups close with the Escape key
            System.out.println("Close button not found, pressing Escape");
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        }
    }

    // Step 3: choose the Round Trip option
    public void selectRoundTrip() {
        click(roundTripTab);
    }

    // Step 4: choose the From city
    public void selectFromCity(String city) {
        click(fromField);
        type(cityTextBox, city);
        click(suggestionFor(city));   // waits until the matching suggestion appears
    }

    // Step 4: choose the To city
    public void selectToCity(String city) {
        click(toField);
        type(cityTextBox, city);
        click(suggestionFor(city));
    }

    // Used for validations: what is currently shown in the From / To boxes
    public String getFromCityValue() {
        return getValue(fromField);
    }

    public String getToCityValue() {
        return getValue(toField);
    }
}