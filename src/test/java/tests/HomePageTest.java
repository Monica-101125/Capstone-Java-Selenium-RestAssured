package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;
import pages.HomePage;

public class HomePageTest extends BaseTest {

    @Test
    public void selectRoundTripAndCities() {
        HomePage home = new HomePage(driver);

        // City names come from config.properties, not hardcoded
        String fromCity = config.getProperty("from.city");
        String toCity = config.getProperty("to.city");

        home.closeLoginPopupIfPresent();
        home.selectRoundTrip();

        home.selectFromCity(fromCity);
        Assert.assertTrue(home.getFromCityValue().toLowerCase().contains(fromCity.toLowerCase()),
                "From city not selected. Actual value: " + home.getFromCityValue());

        home.selectToCity(toCity);
        Assert.assertTrue(home.getToCityValue().toLowerCase().contains(toCity.toLowerCase()),
                "To city not selected. Actual value: " + home.getToCityValue());
    }
}