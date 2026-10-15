# TestDataManager

A Java utility that demonstrates **data-driven test data management** for QA
automation: it reads test data from Excel and configuration from JSON, then
applies filtering and produces statistics.

Built as a hands-on exercise in core QA automation skills — OOP design, external
data sources, and clean Git workflow.

---

## What it does

- Reads **test users** from `test_users.xlsx` (Apache POI)
- Reads **configuration** from `config.json` and **test scenarios** from
  `test_scenarios.json` (Jackson 3)
- **Filters** data (e.g. admin users only) and **counts** statistics
  (users by role, scenarios by category, high-priority scenarios)
- Prints a formatted report to the console

### Sample output

```
CONFIGURATION (from config.json):
  Base URL : https://www.saucedemo.com
  Browser  : chrome
  Timeout  : 30 seconds
  Retries  : 2

TEST USERS (from test_users.xlsx)   -> 12 users
FILTERED: ADMIN USERS ONLY          -> 4 admins
STATISTICS: USERS BY ROLE           -> admin : 4, customer : 8
STATISTICS: SCENARIOS BY CATEGORY   -> 14 scenarios across 8 categories
High-priority scenarios: 6 of 14
```

---

## Tech stack

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21 (LTS) | Language |
| Maven | 3.10 | Build & dependency management |
| Apache POI | 5.5.1 | Read/write Excel (.xlsx) |
| Jackson | 3.2.3 | Read JSON |

---

## Project structure

```
TestDataManager/
├── pom.xml                         Maven config (dependencies, build)
├── .mvn/maven.config               Build-tool configuration
├── src/main/
│   ├── java/com/qa/data/
│   │   ├── DataProvider.java       Interface: the read contract
│   │   ├── TestUser.java           Model: one spreadsheet row
│   │   ├── ExcelDataProvider.java  Reads test_users.xlsx (POI)
│   │   ├── TestConfig.java         Model: config.json
│   │   ├── TestScenario.java       Model: one test scenario
│   │   ├── JsonConfigReader.java   Reads JSON (Jackson 3)
│   │   └── TestDataManager.java    Main class: coordinates + stats
│   └── resources/
│       ├── test_users.xlsx         12 users (admin + customer)
│       ├── config.json             baseUrl, browser, timeout, retries
│       └── test_scenarios.json     14 scenarios (category, type, priority)
└── docs/                           Learning & reference documentation
    ├── structure.md                Structure, diagrams, task list
    ├── code.md                     Line-by-line code explanations
    ├── fix.md                      Every fix/decision + official docs
    └── chat.md                     Project build log
```

OOP design: `ExcelDataProvider` implements the `DataProvider` interface, so the
rest of the code depends on the *contract*, not on Excel specifically — swap the
data source later without changing callers.

---

## How to run

Prerequisites: **JDK 21** and **Maven 3.10+** installed.

```bash
# 1. Compile
mvn -B compile

# 2. Run (offline mode uses the cached libraries)
mvn -o exec:java
```

> `-o` runs Maven offline. Run `mvn compile` once online first so dependencies
> are cached, then `-o` avoids remote lookups. The main class is configured in
> `pom.xml` (`exec.mainClass`).

---

## Branching strategy

This repo uses **GitHub Flow**:

- `main` is the default branch and is always in a releasable state.
- Each change is made on a short-lived branch (`feature/...`, `fix/...`,
  `docs/...`), then merged back via a Pull Request.
- Feature branches are deleted after merge.

See [`docs/branching.md`](docs/branching.md) for the full strategy, including the
important distinction between **branches** and **deployment environments**
(dev/test/uat/prod), and how test automation fits a CI/CD pipeline.

---

## Future enhancements

- Add a `DatabaseDataProvider` implementing the same `DataProvider` interface
  (demonstrates the value of programming to an interface).
- Push results to a test management tool (e.g. **TestRail**) via its REST API.
- Add JUnit tests and run them automatically in a **GitHub Actions** CI pipeline.
- Export the statistics report to CSV/HTML.

---

## What this project demonstrates

- **OOP:** interface, implementations, model classes, encapsulation
- **External data:** reading Excel and JSON into typed Java objects
- **Core logic:** loops, conditions, collections (`ArrayList`, `Map`), statistics
- **Build tooling:** Maven dependency management
- **Version control:** meaningful commits, feature branches, clean history
- **QA mindset:** decoupling test data from logic, verifying integrity, reading
  official docs
