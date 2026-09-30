package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;

public class LaunchTest extends BaseTest {

    // Smoke test: proves Java + Maven + Selenium + TestNG are all wired up
    @Test
    public void verifyPortalLaunches() {
        String title = driver.getTitle();
        System.out.println("Page title: " + title);
        Assert.assertTrue(title.toLowerCase().contains("makemytrip"),
                "Portal did not launch. Actual title: " + title);
    }
}
