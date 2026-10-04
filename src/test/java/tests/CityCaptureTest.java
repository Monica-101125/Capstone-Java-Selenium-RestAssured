package tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.BaseTest;
import pages.HomePage;
import utils.ExcelUtil;

public class CityCaptureTest extends BaseTest {

    private static final String[] HEADER = { "City Code", "City", "Airport Name" };

    @Test
    public void captureCitiesToExcel() throws IOException {
        HomePage home = new HomePage(driver);

        // Search texts and file path come from config.properties, not hardcoded
        List<String> seeds = Arrays.asList(config.getProperty("city.search.seeds").split(","));
        String excelPath = config.getProperty("cities.excel.path");

        System.out.println("Seeds used: " + seeds);

        home.closeLoginPopupIfPresent();
        home.selectRoundTrip();

        List<String[]> source = home.captureFromCities(seeds);
        List<String[]> destination = home.captureToCities(seeds);

        System.out.println("Source cities captured: " + source.size());
        System.out.println("Destination cities captured: " + destination.size());

        Assert.assertFalse(source.isEmpty(), "No source cities were captured");
        Assert.assertFalse(destination.isEmpty(), "No destination cities were captured");

        // Tab 1: source cities. Tab 2: destination cities.
        ExcelUtil.writeSheet(excelPath, "Source Cities", withHeader(source));
        ExcelUtil.writeSheet(excelPath, "Destination Cities", withHeader(destination));

        // Validation: read the file back and compare row counts (+1 for the header row)
        Assert.assertEquals(ExcelUtil.readSheet(excelPath, "Source Cities").size(), source.size() + 1,
                "Source sheet row count mismatch");
        Assert.assertEquals(ExcelUtil.readSheet(excelPath, "Destination Cities").size(), destination.size() + 1,
                "Destination sheet row count mismatch");
    }

    private List<String[]> withHeader(List<String[]> rows) {
        List<String[]> all = new ArrayList<>();
        all.add(HEADER);
        all.addAll(rows);
        return all;
    }
}