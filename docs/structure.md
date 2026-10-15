# TestDataManager — Project Structure & Task List

A reference you can use on the job. Keep it open while you work.

---

## 1. Project Structure Diagram

```
TestDataManager/                         ← repo root
│
├── pom.xml                              ← Maven config: dependencies + build settings
├── .gitignore                           ← files Git must NEVER track (target/, IDE, OS)
├── README.md                            ← portfolio front page (what/why/how to run)
│
├── .mvn/
│   └── maven.config                     ← build-tool fix (disables Maven 3.10 prefix stall)
│
├── docs/                                ← reference & portfolio documentation
│   ├── structure.md                     ← THIS file (structure + task list)
│   ├── chat.md                          ← the full learning conversation thread
│   ├── fix.md                           ← every fix/change, with explanation + official docs
│   └── code.md                          ← line-by-line explanation of every code file
│
├── src/main/
│   ├── java/com/qa/data/                ← our package: com.qa.data
│   │   ├── TestUser.java                ← MODEL (POJO): holds one user row
│   │   ├── DataProvider.java            ← INTERFACE: the read/write contract
│   │   ├── ExcelDataProvider.java       ← reads test_users.xlsx (Apache POI)
│   │   ├── JsonConfigReader.java        ← reads config.json + test_scenarios.json (Jackson)
│   │   └── TestDataManager.java         ← MAIN class: coordinates everything, prints stats
│   │
│   └── resources/                       ← DATA files (decoupled from code)
│       ├── test_users.xlsx              ← 10+ rows: email,password,firstName,lastName,role,expectedResult
│       ├── config.json                  ← baseUrl, browser, timeout, retries
│       └── test_scenarios.json          ← array of test scenarios
│
└── target/                             ← Maven BUILD OUTPUT (git-ignored, auto-generated)
```

### Why this layout
- `src/main/java` vs `src/main/resources`: Maven's **standard directory layout**. Code compiles
  from `java/`; data/config live in `resources/` and are copied to the classpath as-is.
  This separation is the core QA lesson: **test data and config are decoupled from logic**.
- One package `com.qa.data` keeps related classes together.

---

## 2. OOP Design (how the classes relate)

```
            ┌─────────────────────┐
            │   <<interface>>     │
            │    DataProvider     │   the CONTRACT
            │  + readData()       │
            │  + getSourceName()  │
            └──────────▲──────────┘
                       │ implements
            ┌──────────┴──────────┐
            │  ExcelDataProvider  │   reads test_users.xlsx -> List<TestUser>
            └─────────────────────┘

   TestUser          JsonConfigReader          TestDataManager (main)
   (data model)      (reads JSON config)       (coordinates + prints stats)
                        │  reads into
                        ▼
   TestConfig (config.json model)   +   TestScenario (one test_scenarios.json entry)
```

---

## 2b. Dependency Direction & Build Order

Which class needs which. An arrow means "needs / calls".

```
TestDataManager  ──needs──>  ExcelDataProvider  ──needs──>  TestUser
   (main /                ──needs──>  JsonConfigReader   ──needs──>  TestConfig, TestScenario
   conductor)             ──needs──>  DataProvider (interface)
```

**Why TestDataManager is built LAST even though it is the center:**
It depends on everything else. The conductor cannot conduct an orchestra that does
not exist yet. So we build **bottom-up** — foundation first, conductor last.

| Build order | Class(es) | Reason |
|---|---|---|
| 1st | TestUser, DataProvider | No dependencies — pure foundation |
| 2nd | ExcelDataProvider, JsonConfigReader | Depend only on the models/interface |
| last | TestDataManager | Depends on all of the above |

"Center" = importance (where the program runs). "Last" = build order (its parts must
exist first). Like a car: the driver's seat is central, but it is installed into a
frame that already has wheels and an engine.

---

## 3. Task List (build order)

| # | Task | Concept it teaches | Status |
|---|------|--------------------|--------|
| 0 | Install Maven, verify tools (Java, Git, gh) | Environment setup, checksum verification | ✅ done |
| 1 | pom.xml + .gitignore + folders | Maven project, dependency management | ✅ done |
| 2 | TestUser model (POJO) | Encapsulation, constructors, getters | ✅ done |
| 3 | DataProvider interface | Programming to an interface | ✅ done |
| 4 | git init + first commit | Version control basics | ✅ done |
| 5 | Generate test_users.xlsx (12 rows) | Apache POI writing Excel | ✅ done |
| 6 | ExcelDataProvider (implements DataProvider) | POI reading, interface implementation | ✅ done |
| 7 | config.json + test_scenarios.json | JSON data files | ✅ done |
| 8 | JsonConfigReader (Jackson 3.x) | JSON deserialization | ✅ done |
| 9 | TestDataManager main class | Loops, conditions, ArrayList, statistics | ⬜ next |
| 10 | README.md + docs/branching.md | Documentation for portfolio | ✅ done |
| 11 | Feature branch + merge | Git branching workflow (GitHub Flow) | ✅ done |
| 12 | Push to GitHub (rwell10-Red) | Remote repos | ✅ done |

**GitHub repo:** https://github.com/rwell10-Red/TestDataManager (public)

---

## 4. Everyday commands (Windows PowerShell)

```powershell
# make sure Maven is on PATH for this terminal (new terminals pick it up automatically)
$env:Path += ";$env:USERPROFILE\tools\apache-maven-3.10.0\bin"

mvn -B compile          # compile the code
mvn -o exec:java        # RUN the program offline (main class set in pom.xml)
                        #   -o = offline (use cached libs; avoids the remote-lookup stall)
                        #   first time only, run a new plugin online once to cache it
mvn -B clean            # delete target/ (fresh start)
mvn -B package          # build a runnable .jar into target/

git status              # what changed
git add <files>         # stage specific files
git commit -m "msg"     # save a snapshot
git log --oneline       # view history
```
