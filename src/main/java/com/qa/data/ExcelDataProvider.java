package com.qa.data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ExcelDataProvider reads test users from an .xlsx spreadsheet using Apache POI.
 *
 * It IMPLEMENTS the DataProvider interface, meaning it promises to supply real
 * bodies for readData() and getSourceName(). Test code can therefore depend on
 * the DataProvider contract and never needs to know Excel is involved.
 *
 * The spreadsheet layout (matching src/main/resources/test_users.xlsx):
 *   Row 0  : header (email, password, firstName, lastName, role, expectedResult)
 *   Row 1+ : one test user per row
 */
public class ExcelDataProvider implements DataProvider {

    // Column index constants make the code self-documenting (0-based).
    private static final int COL_EMAIL = 0;
    private static final int COL_PASSWORD = 1;
    private static final int COL_FIRST_NAME = 2;
    private static final int COL_LAST_NAME = 3;
    private static final int COL_ROLE = 4;
    private static final int COL_EXPECTED_RESULT = 5;

    /** Classpath resource name of the spreadsheet, e.g. "test_users.xlsx". */
    private final String resourceName;

    public ExcelDataProvider(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Reads every data row from the spreadsheet into a List of TestUser objects.
     * The header row (index 0) is skipped.
     */
    @Override
    public List<TestUser> readData() {
        List<TestUser> users = new ArrayList<>();

        // Load the file from the classpath (src/main/resources is on the classpath).
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Could not find resource on classpath: " + resourceName);
            }

            // WorkbookFactory auto-detects the format (.xlsx here) and opens it.
            try (Workbook workbook = WorkbookFactory.create(in)) {
                Sheet sheet = workbook.getSheetAt(0);     // first worksheet
                DataFormatter formatter = new DataFormatter(); // reads any cell as its displayed text

                // Loop from row 1 to skip the header at row 0.
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        continue; // skip blank rows defensively
                    }

                    String email          = cellText(formatter, row, COL_EMAIL);
                    String password       = cellText(formatter, row, COL_PASSWORD);
                    String firstName      = cellText(formatter, row, COL_FIRST_NAME);
                    String lastName       = cellText(formatter, row, COL_LAST_NAME);
                    String role           = cellText(formatter, row, COL_ROLE);
                    String expectedResult = cellText(formatter, row, COL_EXPECTED_RESULT);

                    // Skip fully-empty rows (e.g. trailing blanks).
                    if (email.isEmpty() && firstName.isEmpty() && lastName.isEmpty()) {
                        continue;
                    }

                    users.add(new TestUser(email, password, firstName, lastName, role, expectedResult));
                }
            }
        } catch (Exception e) {
            // Wrap any IO/format error in a clear runtime exception for the caller.
            throw new RuntimeException("Failed to read Excel data from " + resourceName, e);
        }

        return users;
    }

    @Override
    public String getSourceName() {
        return resourceName;
    }

    /** Safely read one cell as trimmed text (handles null cells). */
    private String cellText(DataFormatter formatter, Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }
}
