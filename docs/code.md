# Code Explanations (line-by-line)

Every code file in the project, explained. Updated as we add files.

---

## Reference Docs (official)

### Java language & built-in classes (the "cheat sheet")
Java is not terminal "commands" — it is classes with methods. Look up a class to
see its methods.

- Java 21 API reference (all built-in classes): https://docs.oracle.com/en/java/javase/21/docs/api/
- Java Tutorials (learning-style): https://docs.oracle.com/javase/tutorial/
- Built-in types we use:
  - `List`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html
  - `ArrayList`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html
  - `Map`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html
  - `LinkedHashMap`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashMap.html
  - `String`: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html
  - `System.out` (printf/println): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/PrintStream.html

### Library methods (the external jars Maven downloaded)
- Apache POI (Excel) API: https://poi.apache.org/apidocs/
  - `WorkbookFactory`, `Workbook`, `Sheet`, `Row`, `Cell`, `DataFormatter` live under
    `org.apache.poi.ss.usermodel`.
  - POI quick guide: https://poi.apache.org/components/spreadsheet/quick-guide.html
- Jackson 3 (JSON): https://github.com/FasterXML/jackson
  - Migration/overview: https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md
  - `JsonMapper.readValue(...)` is the main deserialization method.

### Build & tooling
- Maven: https://maven.apache.org/guides/
- Git: https://git-scm.com/doc

---

## File: `src/main/java/com/qa/data/TestUser.java`

A **model class** (POJO = Plain Old Java Object). One `TestUser` object = one row
of `test_users.xlsx`.

```java
package com.qa.data;
```
Declares the package. A package is a namespace/folder for related classes. The folder
path on disk (`src/main/java/com/qa/data`) must match this.

```java
public class TestUser {
```
Declares a public class named `TestUser`. `public` = usable from anywhere.

```java
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String role;
    private final String expectedResult;
```
Six **fields** (the data each object holds).
- `private` = encapsulation: only code inside this class can touch these directly.
- `final` = once set in the constructor, the value can't be reassigned (immutable object —
  safer, since test data shouldn't change after being read).
- `String` = the data type (text).

```java
    public TestUser(String email, String password, String firstName,
                    String lastName, String role, String expectedResult) {
        this.email = email;
        ...
    }
```
The **constructor**: the special method that runs when you write `new TestUser(...)`.
It receives six values (parameters) and copies them into the object's fields.

**`this.email = email;` explained:** there are two `email` names in scope here — the
field (belongs to the object) and the parameter (passed into the constructor). They
share a name, so `email` alone refers to the *parameter*. `this.email` means "the
field named email on THIS object." So the line reads: "set this object's `email`
field to the value passed in." Without `this.`, you'd be assigning the parameter to
itself (a no-op). So: the field is declared by `private final String email;`, and the
constructor line gives it its value. Your understanding was correct — the only nuance
is that `this.` disambiguates field-vs-parameter.

```java
    public String getEmail() { return email; }
    ... (one getter per field)
```
**Getters**: controlled read-access to private fields. The outside world calls
`user.getEmail()` to read the value but cannot change it. This is encapsulation in
practice. (You had this right: the field *declares/stores*, the getter *retrieves*.)

```java
    public String getFullName() {
        return firstName + " " + lastName;
    }
```
A small convenience method that computes a value (first + space + last) rather than
storing it.

---

## File: `src/main/java/com/qa/data/DataProvider.java`

An **interface** — the contract any data source must fulfill.

```java
package com.qa.data;
import java.util.List;
```
Same package. `import java.util.List` lets us use the `List` type (an ordered collection).

```java
public interface DataProvider {
```
Declares an interface (not a class). It defines *what*, not *how*.

```java
    List<TestUser> readData();
```
An **abstract method signature**: any class implementing `DataProvider` must provide a
`readData()` that returns a `List` of `TestUser` objects. Note the semicolon and no
`{ }` body — that's what "abstract" means in an interface (per Oracle's Java Tutorial).
Interface methods are implicitly `public`.

```java
    String getSourceName();
```
Second contract method: return a human-readable source (e.g. the file name) for logging.

```java
}
```
End of interface.

**Why an interface here:** test code can depend on `DataProvider` instead of a specific
format. Today it's Excel; swap in a database tomorrow and the test code is unchanged.
This is "programming to an interface," a core design principle.

---

## File: `src/main/java/com/qa/data/ExcelDataProvider.java`

Reads `test_users.xlsx` into `List<TestUser>` using Apache POI. **Implements**
the `DataProvider` interface.

```java
package com.qa.data;
import org.apache.poi.ss.usermodel.*;   // Cell, Row, Sheet, Workbook, WorkbookFactory, DataFormatter
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
```
Imports: POI's spreadsheet types, plus `InputStream` (to read the file) and
`ArrayList`/`List` (to collect results).

```java
public class ExcelDataProvider implements DataProvider {
```
`implements DataProvider` = this class promises to provide `readData()` and
`getSourceName()`. If we forgot one, the compiler would reject the class.

```java
    private static final int COL_EMAIL = 0;  // ... COL_EXPECTED_RESULT = 5;
```
Named constants for column indexes. `row.getCell(COL_ROLE)` reads better than
`row.getCell(4)` and prevents off-by-one mistakes. `static final` = one shared,
unchangeable value.

```java
    private final String resourceName;
    public ExcelDataProvider(String resourceName) { this.resourceName = resourceName; }
```
We pass the file name (e.g. `"test_users.xlsx"`) into the constructor, so the same
class can read any spreadsheet with this layout.

```java
    @Override
    public List<TestUser> readData() {
        List<TestUser> users = new ArrayList<>();
```
`@Override` tells the compiler "this fulfills an interface method" (it will error if
the signature doesn't match). We start with an empty `ArrayList` to collect users.

```java
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) { throw new IllegalStateException("Could not find resource ..."); }
```
`getResourceAsStream` loads the file from the **classpath** (Maven copies
`src/main/resources` onto the classpath). `try (...)` is **try-with-resources**: it
auto-closes the stream even if an error happens. We fail fast if the file is missing.

```java
            try (Workbook workbook = WorkbookFactory.create(in)) {
                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter();
```
`WorkbookFactory.create` auto-detects the format and opens it. `getSheetAt(0)` = first
worksheet. `DataFormatter` reads any cell as the text you'd see in Excel (handles
numbers/dates safely so we don't crash on type mismatches).

```java
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
```
**Loop** over data rows. Start at `r = 1` to **skip the header** (row 0). Skip null
(blank) rows defensively.

```java
                    String email = cellText(formatter, row, COL_EMAIL);
                    ... (one per column)
                    if (email.isEmpty() && firstName.isEmpty() && lastName.isEmpty()) continue;
                    users.add(new TestUser(email, password, firstName, lastName, role, expectedResult));
```
Read all six cells, skip fully-empty rows (**condition**), then build one `TestUser`
and add it to the list. This is where one spreadsheet row becomes one object.

```java
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel data from " + resourceName, e);
        }
        return users;
```
Any IO/format error is wrapped in a clear `RuntimeException` (keeps the original cause).
Finally we return the filled list.

```java
    @Override public String getSourceName() { return resourceName; }
```
The second contract method: reports the source file name (handy in test logs).

```java
    private String cellText(DataFormatter formatter, Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }
```
Helper: safely reads one cell as trimmed text, returning `""` for missing cells so the
caller never hits a null.

---

## File: `src/main/java/com/qa/data/TestConfig.java`

Model for `config.json`. One object holds the whole config file.

```java
private String baseUrl;
private String browser;
private int timeout;
private int retries;
```
Four fields whose **names match the JSON keys** exactly. That name-matching is how
Jackson knows which JSON value goes into which field.

```java
public TestConfig() {}
```
A **no-argument constructor**. Jackson creates an empty object first, then fills the
fields via setters, so it needs this.

```java
public String getBaseUrl() { return baseUrl; }   // + the other getters
public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }  // + the other setters
```
Getters for our code to read values; setters for Jackson to populate them during
deserialization.

```java
@Override public String toString() { ... }
```
Returns a readable one-line summary, handy for printing/logging.

---

## File: `src/main/java/com/qa/data/TestScenario.java`

Model for ONE entry in `test_scenarios.json` (the file is an array of these).

```java
private String id;        // e.g. TC_LOGIN_001
private String name;      // human-readable scenario title
private String category;  // FEATURE area: login, forgotPassword, session, ...
private String type;      // TEST TYPE: functional, negative, security, performance, ...
private String priority;  // High / Medium / Low
```
Two categorization axes live here: `category` (feature) and `type` (test type) — the
exact distinction we discussed. Plus `priority` for business urgency.

No-arg constructor, getters, setters, and `toString()` — same pattern as TestConfig.

---

## File: `src/main/java/com/qa/data/JsonConfigReader.java`

Reads both JSON files using **Jackson 3.x**.

```java
import tools.jackson.databind.json.JsonMapper;
```
Jackson 3's package is `tools.jackson.*` (Jackson 2 used `com.fasterxml.jackson.*`).

```java
private final JsonMapper mapper = JsonMapper.builder().build();
```
`JsonMapper` is Jackson 3's recommended immutable mapper, created via a builder. One
shared instance is reused for all reads (mappers are thread-safe and reusable).

```java
public TestConfig readConfig(String resourceName) {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
        if (in == null) throw new IllegalStateException("Could not find resource ...");
        return mapper.readValue(in, TestConfig.class);
    } catch (Exception e) {
        throw new RuntimeException("Failed to read JSON config from " + resourceName, e);
    }
}
```
Loads `config.json` from the classpath, then `readValue(in, TestConfig.class)` turns the
JSON object into one `TestConfig`. **Deserialization** = JSON text → Java object. The
try-with-resources auto-closes the stream; errors are wrapped with a clear message.

```java
public List<TestScenario> readScenarios(String resourceName) {
    ...
    TestScenario[] array = mapper.readValue(in, TestScenario[].class);
    return List.of(array);
}
```
`test_scenarios.json` is a JSON **array**, so we deserialize into a `TestScenario[]`
(array) and wrap it in an immutable `List`. Note: no `throws IOException` is needed —
Jackson 3 uses unchecked exceptions.

---

## File: `src/main/java/com/qa/data/TestDataManager.java`

The MAIN class — the "conductor". Built LAST because it depends on every other class
(see docs/structure.md section 2b). Run it with: `mvn -o exec:java`.

```java
package com.qa.data;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
```
Imports the collections we use: `ArrayList`/`List` (ordered collection) and
`Map`/`LinkedHashMap` (key→value pairs that remember insertion order).

```java
public static void main(String[] args) {
    TestDataManager app = new TestDataManager();
    app.run();
}
```
`main` is where Java starts the program (the launcher calls `public static void main`).
It makes one TestDataManager object and calls `run()`. (Keeping logic in `run()` instead
of `main` lets the instance methods use `this` naturally.)

### STEP 1 — read config.json
```java
JsonConfigReader jsonReader = new JsonConfigReader();
TestConfig config = jsonReader.readConfig("config.json");
System.out.println("  Base URL : " + config.getBaseUrl());
```
Make a reader, call `readConfig(...)` to get a `TestConfig` object, then print its fields
with the getters. `System.out.println` prints a line.

### STEP 2 — read users and print them (LOOP #1)
```java
DataProvider excel = new ExcelDataProvider("test_users.xlsx");
List<TestUser> users = excel.readData();
System.out.printf("%-24s %-16s %-10s %-8s%n", "EMAIL", "NAME", "ROLE", "EXPECTED");
for (TestUser user : users) {
    System.out.printf("%-24s %-16s %-10s %-8s%n",
        user.getEmail(), user.getFullName(), user.getRole(), user.getExpectedResult());
}
```
Note the type is `DataProvider` (the interface), not `ExcelDataProvider` — programming to
the interface. `readData()` returns the `List` we loop over. `printf` formats columns:
`%-24s` = left-aligned string in a 24-wide field; `%n` = newline. The `for (TestUser user
: users)` loop runs once per user.

### STEP 3 — filter admins (LOOP + CONDITION + ArrayList)
```java
List<TestUser> admins = new ArrayList<>();          // empty box for results
for (TestUser user : users) {                        // LOOP: each user
    if (user.getRole().equalsIgnoreCase("admin")) {  // CONDITION: role is admin?
        admins.add(user);                            // YES -> put in the box
    }
}
```
This is the core pattern. `equalsIgnoreCase` compares text ignoring upper/lowercase.
`admins.add(user)` appends to the list. After the loop, `admins` holds only admins.

### STEP 4 — statistics: users by role (a Map as a tally)
```java
Map<String, Integer> usersByRole = new LinkedHashMap<>();
for (TestUser user : users) {
    String role = user.getRole();
    usersByRole.put(role, usersByRole.getOrDefault(role, 0) + 1);
}
```
A `Map` stores `role -> count`. For each user: `getOrDefault(role, 0)` returns the current
count (or 0 the first time we see a role), we add 1, and `put` saves it back. Result:
`admin=4, customer=8`.

```java
for (Map.Entry<String, Integer> entry : usersByRole.entrySet()) {
    System.out.println("  " + entry.getKey() + " : " + entry.getValue());
}
```
Loop over the map's entries; `getKey()` is the role, `getValue()` is the count.

### STEP 5 — scenarios by category (same tally pattern)
```java
List<TestScenario> scenarios = jsonReader.readScenarios("test_scenarios.json");
Map<String, Integer> scenariosByCategory = new LinkedHashMap<>();
for (TestScenario scenario : scenarios) {
    String category = scenario.getCategory();
    scenariosByCategory.put(category, scenariosByCategory.getOrDefault(category, 0) + 1);
}
```
Read the 14 scenarios, then tally by `category` (feature area) exactly like Step 4.

### STEP 6 — count high-priority scenarios (another filter, with a counter)
```java
int highPriorityCount = 0;
for (TestScenario scenario : scenarios) {
    if (scenario.getPriority().equalsIgnoreCase("High")) {
        highPriorityCount++;
    }
}
```
Instead of collecting into a list, this just increments an `int` counter when the
condition is true. `highPriorityCount++` means "add 1".

### Helper
```java
private void printBanner(String title) {
    System.out.println("=".repeat(62));
    System.out.println("  " + title);
    System.out.println("=".repeat(62));
}
```
A small reusable method for the titled dividers. `"=".repeat(62)` makes a line of 62
equals signs. Defining it once avoids repeating the three print lines everywhere.
