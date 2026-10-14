# Chat Thread — TestDataManager Learning Log

A record of our working conversation while building this project, kept for the
portfolio and for your own reference. Newest decisions appended over time.

---

## Session 1 — Setup & foundations

**Goal:** Build a Test Data Management utility in Java that reads test data from
Excel and config from JSON, while learning QA skills step by step.

**Working style agreed:** Teach step by step, short answers, always check official
docs before designing/running. Start step-by-step, transition to hybrid later.

### What we did
1. **Checked tooling:** Java 21 ✅, Git 2.55 ✅, GitHub CLI 2.98 ✅ (logged in as
   `rwell10-Red`), Maven ❌ (installed it manually — see docs/fix.md FIX-001).
2. **Installed Maven 3.10.0** from the official binary zip, verified SHA-512 checksum.
3. **Scaffolded the project:** `pom.xml` (POI 5.5.1 + Jackson), `.gitignore`,
   `src/main/java/com/qa/data`, `src/main/resources`.
4. **Fixed a Maven 3.10 stall** via `.mvn/maven.config` (see docs/fix.md FIX-002).
5. **Wrote `TestUser.java`** (model/POJO) and **`DataProvider.java`** (interface).
6. **git init + first commit** on branch `main`.
7. **Upgraded Jackson to 3.2.3** (new groupId `tools.jackson.core`) — see fix.md CHANGE-001.

### Key Q&A (answered in chat)
- **Is WSL needed?** No — native Windows toolchain is complete.
- **Why verify checksums?** Integrity/trust; corrupted tools invalidate test results.
- **What is a checksum / SHA-512?** A file fingerprint; SHA-512 = 512-bit cryptographic hash.
- **Why does Maven run on Java?** Maven is itself a Java application.
- **Other automation languages?** Python, JavaScript/TypeScript, C#, Ruby, etc.
- **Where do Jackson & POI come from?** Maven Central; cached in `~/.m2/repository`.
- **How does Maven know what to fetch?** From `<dependencies>` in `pom.xml` +
  transitive deps resolved from each library's own POM.
- **Is there a Maven UI/account?** No account needed; Maven is a CLI tool. Browse
  libraries at https://central.sonatype.com.

### Next up
- Generate `test_users.xlsx` (10+ rows) with Apache POI.
- Build `ExcelDataProvider` to read it back (implements `DataProvider`).

> The detailed, full-length answers to the questions above also live inline in the
> chat and are distilled into docs/fix.md and docs/code.md.
