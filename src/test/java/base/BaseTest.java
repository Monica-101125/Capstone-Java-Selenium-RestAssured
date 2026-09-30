package base;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    protected WebDriver driver;
    protected Properties config;

    // Runs BEFORE every @Test method (like opening a fresh browser for each test case)
    @BeforeMethod
    public void setUp() throws IOException {
        config = loadConfig();

        // Reads -Dbrowser=... from the command line. Defaults to chrome.
        String browser = System.getProperty("browser", "chrome");

        switch (browser.toLowerCase()) {
            case "chrome":
                driver = new ChromeDriver();
                break;
            case "firefox":
                driver = new FirefoxDriver();
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        driver.manage().window().maximize();

        // Implicit wait: Selenium keeps looking for an element up to N seconds before failing
        int implicitWait = Integer.parseInt(config.getProperty("implicit.wait"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        driver.get(config.getProperty("base.url"));
    }

    // Runs AFTER every @Test method (closes the browser, even if the test failed)
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Reads values from src/test/resources/config.properties
    private Properties loadConfig() throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IOException("config.properties not found in src/test/resources");
            }
            props.load(in);
        }
        return props;
    }
}
