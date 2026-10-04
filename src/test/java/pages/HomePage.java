package pages;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
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

    // Search widget (names come from the page's own data-cy labels)
    private final By departureField  = By.cssSelector("[data-cy='departure']");
    private final By returnField     = By.cssSelector("[data-cy='return']");
    private final By departureShown  = By.cssSelector("[data-cy='departureDate']");
    private final By returnShown     = By.cssSelector("[data-cy='returnDate']");
    private final By nextMonthButton = By.xpath("//span[@aria-label='Next Month']");
    private final By travellersBox   = By.cssSelector("[data-cy='travellers']");
    private final By travellersClick = By.cssSelector("label[for='travellers']");
    private final By travellersShown = By.cssSelector("[data-cy='flightTravellersOnly']");
    private final By cabinClick      = By.cssSelector("label[for='cabinClass']");
    private final By cabinShown      = By.cssSelector("label[for='cabinClass'] .cabinClassLabel");
    private final By regularFare     = By.xpath("//div[contains(@class,'fareCardItem')][.//div[normalize-space(text())='Regular']]");
    private final By cabinClassBox   = By.cssSelector("[data-cy='cabinClass']");
    private final By searchButton    = By.cssSelector("[data-cy='submit']");

    // The calendar labels each day like "Wed Nov 04 2026"
    private static final DateTimeFormatter CALENDAR_LABEL =
            DateTimeFormatter.ofPattern("EEE MMM dd yyyy", Locale.ENGLISH);

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

    // ---------- Steps 6 to 9: dates, fare type, travellers and class, search ----------

    public void selectDepartureDate(LocalDate date) {
        selectDate(departureField, date);
    }

    public void selectReturnDate(LocalDate date) {
        selectDate(returnField, date);
    }

    // Step 6: pick a date in the calendar. The calendar shows two months at a time,
    // so click "next month" only if the date isn't visible yet.
    private void selectDate(By dateField, LocalDate date) {
        String dayCell = "div.DayPicker-Day[aria-label='" + date.format(CALENDAR_LABEL) + "']"
                + ":not(.DayPicker-Day--outside)";

        if (!isPresent("div.DayPicker-Day")) {
            click(dateField);                                   // calendar is closed, so open it
        }
        waitForVisible(By.cssSelector("div.DayPicker-Day"));     // wait for it to finish drawing

        for (int i = 0; i < 6 && !isPresent(dayCell); i++) {
            click(nextMonthButton);
        }
        click(By.cssSelector(dayCell));
    }

    // Quick yes/no check that an element exists, without waiting
    private boolean isPresent(String cssSelector) {
        Object found = ((JavascriptExecutor) driver).executeScript(
                "return document.querySelector(arguments[0]) !== null;", cssSelector);
        return Boolean.TRUE.equals(found);
    }

    // Step 7: passenger type "Regular"
    public void selectRegularFare() {
        click(regularFare);
    }

    // Validation: the page marks the chosen fare card with the class "activeList"
    public boolean isRegularFareSelected() {
        return waitForVisible(regularFare).getAttribute("class").contains("activeList");
    }

    // Step 8: number of adults, then cabin class
    public void selectTravellersAndClass(int adults, String cabin) {
        click(travellersClick);                                          // opens the travellers panel
        click(By.cssSelector("li[data-cy='adults-" + adults + "']"));    // e.g. the "1" button
        click(By.cssSelector("button[data-cy='travellerApplyBtn']"));    // APPLY
        selectCabinClass(cabin);
    }

    // The cabin class is a separate box. If it already shows the class we want
    // (the default is "Economy/Premium Economy"), there is nothing to change.
    private void selectCabinClass(String cabin) {
        if (getCabinText().toLowerCase().contains(cabin.toLowerCase())) {
            System.out.println("Cabin class already set to: " + getCabinText());
            return;
        }
        // Not the one we want, so open the box and pick it by its visible name.
        // (Not exercised yet, since the default is Economy. We will check it if we ever need it.)
        click(cabinClick);
        click(By.xpath("//*[self::li or self::p or self::span][contains(normalize-space(text()),'" + cabin + "')]"));
    }

    // Step 9: search for flights
    public void clickSearch() {
        click(searchButton);
    }

    // Used for validations
    public String getDepartureText()  { return waitForVisible(departureShown).getText(); }
    public String getReturnText()     { return waitForVisible(returnShown).getText(); }
    public String getTravellersText() { return waitForVisible(travellersShown).getText(); }
    public String getCabinText()      { return waitForVisible(cabinShown).getText(); }

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

        // The To box opens with a default "popular cities" list (the From box shows nothing).
        // Wait until that default list has finished loading, then remember it, so it can never
        // be mistaken for the result of a search.
        String defaultList = waitForStableList();
        System.out.println("[capture v3] default list on opening: " + (defaultList.isEmpty() ? "(none)" : defaultList));

        for (String seed : seeds) {
            String before = suggestionsSnapshot();          // list on screen BEFORE typing
            typeIntoActiveBox(seed.trim());
            if (!waitForNewList(before, defaultList)) {
                // Nothing new appeared. Type once more and wait again.
                System.out.println("List did not change for '" + seed.trim() + "', typing again");
                typeIntoActiveBox(seed.trim());
                waitForNewList(before, defaultList);
            }
            List<String[]> rows = readSuggestions();
            StringBuilder codes = new StringBuilder();
            for (String[] r : rows) {
                codes.append(r[0]).append(' ');
            }
            System.out.println("Seed '" + seed.trim() + "' returned " + rows.size() + " suggestions: " + codes);
            for (String[] row : rows) {
                unique.putIfAbsent(row[0], row);
            }
        }
        return new ArrayList<>(unique.values());
    }

    // Types into whichever text box currently has the cursor, which is the box that opened
    // when we clicked From or To.
    private void typeIntoActiveBox(String text) {
        wait.until(d -> "input".equalsIgnoreCase(d.switchTo().activeElement().getTagName()));
        WebElement box = driver.switchTo().activeElement();
        box.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        box.sendKeys(text);
        System.out.println("Typed '" + text + "' into box placeholder='" + box.getAttribute("placeholder")
                + "', value now='" + box.getAttribute("value") + "'");
    }

    // Waits until the list on screen has stopped changing (same on two checks in a row).
    // Returns it as a snapshot. Returns "" if no list ever appears (the From box).
    private String waitForStableList() {
        final String[] last = { null };
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(4))
                    .pollingEvery(Duration.ofMillis(400))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        String now = suggestionsSnapshot();
                        boolean stable = !now.isEmpty() && now.equals(last[0]);
                        last[0] = now;
                        return stable;
                    });
        } catch (TimeoutException e) {
            // No default list in this box. That's fine.
        }
        return suggestionsSnapshot();
    }

    // After typing, an OLD list stays for a moment. Wait until the list is a genuinely new one:
    // not empty, not what was there before typing, and not the default list.
    // Returns true if a new list appeared.
    private boolean waitForNewList(String before, String defaultList) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        String now = suggestionsSnapshot();
                        return !now.isEmpty() && !now.equals(before) && !now.equals(defaultList);
                    });
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // A short fingerprint of the list on screen: just the airport codes, joined together.
    // Using codes means small text re-renders are ignored; only a different set of cities counts.
    private String suggestionsSnapshot() {
        Object result = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll(\"ul[role='listbox'] li .revampedIataText\"))"
                        + ".map(function(e){ return e.textContent.trim(); }).join('|');");
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