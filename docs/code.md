# Code Explanations (line-by-line)

Every code file in the project, explained. Updated as we add files.

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
