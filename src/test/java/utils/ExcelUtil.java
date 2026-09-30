package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Reusable Excel helper (Apache POI). Used for scenario step 5 and later for data-driven tests.
public class ExcelUtil {

    // Writes rows into a sheet. Creates the file if it doesn't exist.
    // If the sheet already exists, it is replaced (so re-runs don't pile up old data).
    // Each String[] is one row, and each item in it is one cell.
    public static void writeSheet(String filePath, String sheetName, List<String[]> rows) throws IOException {
        File file = new File(filePath);
        Workbook workbook;

        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(in);
            }
        } else {
            workbook = new XSSFWorkbook();
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();   // create the folder if missing
            }
        }

        int existing = workbook.getSheetIndex(sheetName);
        if (existing >= 0) {
            workbook.removeSheetAt(existing);
        }

        Sheet sheet = workbook.createSheet(sheetName);
        int rowNumber = 0;
        for (String[] rowData : rows) {
            Row row = sheet.createRow(rowNumber++);
            for (int col = 0; col < rowData.length; col++) {
                row.createCell(col).setCellValue(rowData[col]);
            }
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        }
        workbook.close();
    }

    // Reads every row of a sheet back as text. Cell values of any type (number, date, text)
    // come back as the same text you would see in Excel.
    public static List<String[]> readSheet(String filePath, String sheetName) throws IOException {
        List<String[]> data = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream in = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(in)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IOException("Sheet not found: " + sheetName);
            }

            for (Row row : sheet) {
                int lastCell = row.getLastCellNum();
                String[] values = new String[Math.max(lastCell, 0)];
                for (int col = 0; col < lastCell; col++) {
                    Cell cell = row.getCell(col);
                    values[col] = (cell == null) ? "" : formatter.formatCellValue(cell);
                }
                data.add(values);
            }
        }
        return data;
    }
}