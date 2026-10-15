# Roadmap / Backlog

Planned enhancements for TestDataManager, tracked as real work items. The short
"Future enhancements" list in the README is the summary; this file is the detail.

Status legend: ⬜ not started · 🔄 in progress · ✅ done

---

## Priority: High

### R-001 — JUnit tests + GitHub Actions CI
**Status:** ⬜
**Why:** Makes the test-automation lifecycle real — tests run automatically on every
push and gate the build (see docs/branching.md).
**Scope:**
- Add JUnit 5 dependency to `pom.xml`.
- Write unit tests for `ExcelDataProvider` and `JsonConfigReader` (read a known
  fixture, assert the parsed values).
- Add `.github/workflows/ci.yml` that runs `mvn -B test` on push and pull_request.
**Done when:** CI badge is green on `main` and tests run on every PR.
**Docs to read first:**
- JUnit 5: https://junit.org/junit5/docs/current/user-guide/
- Maven Surefire (runs tests): https://maven.apache.org/surefire/maven-surefire-plugin/
- GitHub Actions for Java/Maven: https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-maven

---

## Priority: Medium

### R-002 — DatabaseDataProvider
**Status:** ⬜
**Why:** Demonstrates the value of the `DataProvider` interface — swap the data source
without changing callers.
**Scope:**
- New class `DatabaseDataProvider implements DataProvider`.
- Read users from an embedded DB (e.g. H2 or SQLite) into `List<TestUser>`.
- `TestDataManager` works unchanged because it depends on the interface.
**Done when:** The main class can switch between Excel and DB by changing one line.
**Docs to read first:**
- JDBC basics: https://docs.oracle.com/javase/tutorial/jdbc/
- H2 database: https://www.h2database.com/html/main.html

### R-003 — Export statistics report to CSV / HTML
**Status:** ⬜
**Why:** Shareable output; closer to how real reports are consumed.
**Scope:**
- Write the role/category/priority stats to a `report.csv` and/or `report.html`.
**Done when:** Running the app also produces a report file in `target/`.

---

## Priority: Low / Later

### R-004 — Push results to TestRail via REST API
**Status:** ⬜
**Why:** Mirrors real QA pipelines where automated results feed a test-management tool.
**Scope:**
- After computing stats, POST results to a TestRail run via its REST API.
- Keep credentials out of the repo (environment variables / secrets).
**Note:** Requires a TestRail account; keep this optional/documented, not a hard
dependency of the project.
**Docs to read first:**
- TestRail API: https://support.testrail.com/hc/en-us/articles/7077083596436-Introduction-to-the-TestRail-API

---

## Ground rules for picking up any item
1. Read the official docs listed first (our standing rule).
2. Work on a short-lived branch (`feature/...`), PR into `main`, delete after merge.
3. Record any fix/decision in `docs/fix.md`, and line-by-line code in `docs/code.md`.
4. Verify it actually runs/tests green before marking ✅.
