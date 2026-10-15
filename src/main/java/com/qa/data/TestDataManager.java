package com.qa.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TestDataManager is the MAIN class — the "conductor".
 *
 * It uses the readers we built (ExcelDataProvider, JsonConfigReader) to load data,
 * then demonstrates the four logic requirements:
 *   1. LOOPS      — iterate the user list and the scenario list
 *   2. CONDITIONS — filter (e.g. keep only "admin" users)
 *   3. ArrayList  — store the filtered results
 *   4. STATISTICS — count users by role, count scenarios by category
 *
 * Run it with:  mvn -B exec:java
 */
public class TestDataManager {

    public static void main(String[] args) {
        TestDataManager app = new TestDataManager();
        app.run();
    }

    private void run() {
        printBanner("TEST DATA MANAGER");

        // ===== STEP 1: Load the JSON configuration =====
        JsonConfigReader jsonReader = new JsonConfigReader();
        TestConfig config = jsonReader.readConfig("config.json");

        System.out.println("CONFIGURATION (from config.json):");
        System.out.println("  Base URL : " + config.getBaseUrl());
        System.out.println("  Browser  : " + config.getBrowser());
        System.out.println("  Timeout  : " + config.getTimeout() + " seconds");
        System.out.println("  Retries  : " + config.getRetries());
        System.out.println();

        // ===== STEP 2: Load the test users from Excel =====
        DataProvider excel = new ExcelDataProvider("test_users.xlsx");
        List<TestUser> users = excel.readData();   // the List we loop over below

        printBanner("TEST USERS (from " + excel.getSourceName() + ")");
        System.out.printf("%-24s %-16s %-10s %-8s%n", "EMAIL", "NAME", "ROLE", "EXPECTED");
        System.out.println("-".repeat(62));

        // LOOP #1: visit every user and print it in a formatted row.
        for (TestUser user : users) {
            System.out.printf("%-24s %-16s %-10s %-8s%n",
                    user.getEmail(), user.getFullName(), user.getRole(), user.getExpectedResult());
        }
        System.out.println("\nTotal users: " + users.size());

        // ===== STEP 3: FILTER — keep only the admins (loop + condition + ArrayList) =====
        List<TestUser> admins = new ArrayList<>();     // the empty "box" for results
        for (TestUser user : users) {                  // LOOP: visit each user
            if (user.getRole().equalsIgnoreCase("admin")) {  // CONDITION: is this an admin?
                admins.add(user);                      // YES -> add to the box
            }
        }

        printBanner("FILTERED: ADMIN USERS ONLY");
        for (TestUser admin : admins) {
            System.out.println("  - " + admin.getFullName() + " (" + admin.getEmail() + ")");
        }
        System.out.println("\nAdmin count: " + admins.size());

        // ===== STEP 4: STATISTICS — count users by role =====
        // A Map holds "role -> count" pairs (LinkedHashMap keeps insertion order).
        Map<String, Integer> usersByRole = new LinkedHashMap<>();
        for (TestUser user : users) {
            String role = user.getRole();
            // getOrDefault: current count for this role, or 0 if first time seen, then +1.
            usersByRole.put(role, usersByRole.getOrDefault(role, 0) + 1);
        }

        printBanner("STATISTICS: USERS BY ROLE");
        for (Map.Entry<String, Integer> entry : usersByRole.entrySet()) {
            System.out.println("  " + entry.getKey() + " : " + entry.getValue());
        }

        // ===== STEP 5: Load scenarios and compute statistics by category =====
        List<TestScenario> scenarios = jsonReader.readScenarios("test_scenarios.json");

        Map<String, Integer> scenariosByCategory = new LinkedHashMap<>();
        for (TestScenario scenario : scenarios) {
            String category = scenario.getCategory();
            scenariosByCategory.put(category, scenariosByCategory.getOrDefault(category, 0) + 1);
        }

        printBanner("STATISTICS: SCENARIOS BY CATEGORY");
        System.out.println("Total scenarios: " + scenarios.size() + "\n");
        for (Map.Entry<String, Integer> entry : scenariosByCategory.entrySet()) {
            System.out.println("  " + entry.getKey() + " : " + entry.getValue());
        }

        // ===== STEP 6: Count HIGH priority scenarios (another filter) =====
        int highPriorityCount = 0;
        for (TestScenario scenario : scenarios) {
            if (scenario.getPriority().equalsIgnoreCase("High")) {
                highPriorityCount++;
            }
        }
        System.out.println("\nHigh-priority scenarios: " + highPriorityCount + " of " + scenarios.size());

        printBanner("DONE");
    }

    /** Small helper to print a titled divider so the output is easy to read. */
    private void printBanner(String title) {
        System.out.println();
        System.out.println("=".repeat(62));
        System.out.println("  " + title);
        System.out.println("=".repeat(62));
    }
}
