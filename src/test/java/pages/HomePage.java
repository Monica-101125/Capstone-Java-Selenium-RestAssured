package pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// Represents the MakeMyTrip home page (scenario steps 2 to 5).
// NOTE: Locators are based on the site's HTML today. If the site changes,
// only this file needs fixing.
public class HomePage extends BasePage {

    // ---- Locators (where things are on the page) ----
    private final By closePopupButton = By.xpath("//span[@data-cy='closeModal']");
    private final By roundTripTab     = By.xpath("//li[@data-cy='roundTrip']");
    private final By fromField        = By.id("fromCity");
    private final By toField          = By.id("toCity");
    private final By cityTextBox      = By.xpath("//input[@placeholder='Enter city or airport' or @placeholder='From' or @placeholder='To']");

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

    // Finds the suggestion whose text contains the city we typed (case-insensitive)
    private By suggestionFor(String city) {
        return By.xpath("(//ul[@role='listbox']//li[contains(translate(., "
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + city.toLowerCase() + "')])[1]");
    }

    // Step 4: choose the From city
    public void selectFromCity(String city) {
        click(fromField);
        type(cityTextBox, city);
        click(suggestionFor(city));
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

    // ---------- Step 5: capture cities (code, city, airport) ----------

    // Each row is { city code, city, airport name }
    public List<String[]> captureFromCities(List<String> seeds) {
        return captureCities(fromField, seeds);
    }

    public List<String[]> captureToCities(List<String> seeds) {
        return captureCities(toField, seeds);
    }

    // Types each search text ("seed") into the field and collects every suggestion shown.
    // A Map keyed by city code removes duplicates, since one city appears for many seeds.
    private List<String[]> captureCities(By field, List<String> seeds) {
        Map<String, String[]> unique = new LinkedHashMap<>();
        click(field);
        String previous = "";

        for (String seed : seeds) {
            type(cityTextBox, seed.trim());
            previous = waitForSuggestionsToChange(previous);
            for (String[] row : readSuggestions()) {
                unique.putIfAbsent(row[0], row);
            }
        }
        return new ArrayList<>(unique.values());
    }

    // After typing, the OLD list stays for a moment. Wait until the list text is different.
    private String waitForSuggestionsToChange(String previous) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        String now = suggestionsSnapshot();
                        return !now.isEmpty() && !now.equals(previous);
                    });
        } catch (TimeoutException e) {
            // The list may legitimately be identical to the last one. Carry on.
        }
        return suggestionsSnapshot();
    }

    // All suggestion text joined into one string, used only to detect that the list changed
    private String suggestionsSnapshot() {
        Object result = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll(\"ul[role='listbox'] li\"))"
                        + ".map(function(li){ return li.textContent; }).join('|');");
        return result == null ? "" : result.toString();
    }

    // Reads code, city and airport from every suggestion in one go (JavaScript reads them
    // all at once, so the list can't change halfway through reading).
    @SuppressWarnings("unchecked")
    private List<String[]> readSuggestions() {
        String script =
                "return Array.from(document.querySelectorAll(\"ul[role='listbox'] li\")).map(function(li) {"
                        + "  function q(sel) { var e = li.querySelector(sel); return e ? e.textContent.trim() : ''; }"
                        + "  return [q('.revampedIataText'), q('.revampedCityName'), q('.revampedAirportName'),"
                        + "          li.querySelector('.revampedGroupDataItem') ? 'nearby' : 'main'];"
                        + "});";

        List<List<Object>> raw = (List<List<Object>>) ((JavascriptExecutor) driver).executeScript(script);
        List<String[]> rows = new ArrayList<>();
        if (raw == null) {
            return rows;
        }

        String lastMainCity = "";
        for (List<Object> item : raw) {
            String code = String.valueOf(item.get(0));
            String name = String.valueOf(item.get(1));
            String detail = String.valueOf(item.get(2));
            String type = String.valueOf(item.get(3));

            if (code.isEmpty()) {
                continue;   // not a real airport entry
            }
            if ("main".equals(type)) {
                lastMainCity = name;
                rows.add(new String[] { code, name, detail });        // city, airport name
            } else {
                // "Nearby" entry: its name IS the airport; it belongs to the last main city
                rows.add(new String[] { code, lastMainCity, name });
            }
        }
        return rows;
    }
}