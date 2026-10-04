package tests;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;
import pages.HomePage;

// Scenario steps 1 to 9: launch, popup, round trip, cities, dates, fare, travellers, search
public class SearchFlightsTest extends BaseTest {

    @Test
    public void searchRoundTripFlights() {
        HomePage home = new HomePage(driver);

        // Everything below comes from config.properties, nothing is hardcoded
        String fromCity = config.getProperty("from.city");
        String toCity = config.getProperty("to.city");
        int monthsAhead = Integer.parseInt(config.getProperty("trip.depart.months.ahead"));
        int stayDays = Integer.parseInt(config.getProperty("trip.stay.days"));
        int adults = Integer.parseInt(config.getProperty("travellers.adults"));
        String cabin = config.getProperty("cabin.class");

        // Dates are calculated from today's date: departure is one month ahead
        LocalDate departure = LocalDate.now().plusMonths(monthsAhead);
        LocalDate returnDate = departure.plusDays(stayDays);
        System.out.println("Departure: " + departure + ", Return: " + returnDate);

        home.closeLoginPopupIfPresent();
        home.selectRoundTrip();
        home.selectFromCity(fromCity);
        home.selectToCity(toCity);

        home.selectDepartureDate(departure);
        home.selectReturnDate(returnDate);
        assertDateShown(home.getDepartureText(), departure, "Departure");
        assertDateShown(home.getReturnText(), returnDate, "Return");

        home.selectRegularFare();
        Assert.assertTrue(home.isRegularFareSelected(), "Regular fare was not marked as selected");

        home.selectTravellersAndClass(adults, cabin);
        Assert.assertTrue(home.getTravellersText().contains(String.valueOf(adults)),
                "Travellers not set. Shown: " + home.getTravellersText());
        Assert.assertTrue(home.getCabinText().toLowerCase().contains(cabin.toLowerCase()),
                "Cabin class not set. Shown: " + home.getCabinText());

        home.clickSearch();

        // The results page address contains "search"
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.urlContains("search"));
        System.out.println("Results page: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("search"), "Search results page did not open");
    }

    // Checks that the widget shows the day number and the month we picked
    private void assertDateShown(String shownText, LocalDate date, String label) {
        String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        boolean ok = shownText.contains(String.valueOf(date.getDayOfMonth())) && shownText.contains(month);
        Assert.assertTrue(ok, label + " date not shown correctly. Expected day " + date.getDayOfMonth()
                + " and month " + month + ", but the page shows: '" + shownText + "'");
    }
}