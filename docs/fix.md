# Fixes, Changes & Decisions Log

Every notable fix or change, with the reason and the official documentation that
supports it. This is a portfolio asset: diagnosing and fixing build-tool issues
is exactly what QA/SDET engineers do.

---

## FIX-001 — Maven not installed → manual install from official binary zip

**Problem:** `mvn` was not recognized. Java 21, Git, and GitHub CLI were present.

**Why manual install:** The winget catalog did not list the official `Apache.Maven`
package at install time, so we used the vendor's documented method.

**Official docs:**
- Install guide: https://maven.apache.org/install — "extract the archive and add its
  `bin` directory to the PATH."
- Download page: https://maven.apache.org/download.cgi — current stable **Maven 3.10.0**,
  requires JDK 8+.

**Steps performed:**
1. Downloaded `apache-maven-3.10.0-bin.zip` from the official `dlcdn.apache.org` mirror.
2. Verified the **SHA-512 checksum** against the official `.sha512` file → matched.
3. Extracted to `C:\Users\<you>\tools\apache-maven-3.10.0`.
4. Added `...\apache-maven-3.10.0\bin` to the **user** PATH.
5. Verified with `mvn -version` → Maven 3.10.0 on Java 21.

**What a checksum / SHA-512 is:** A checksum is a short fixed-length "fingerprint"
computed from a file's bytes. SHA-512 is a cryptographic hash producing a 512-bit
(128 hex-char) fingerprint. If even one byte of the download differs, the fingerprint
changes completely. We compare our computed hash to the publisher's published hash.
Match = the file is authentic and not corrupted or tampered with.

**Why verifying integrity is a QA habit:** QA is about trust and repeatability. A
corrupted or tampered build tool invalidates every test result it produces. Verifying
checksums (and signatures) is a basic supply-chain / integrity control — the same
mindset QA applies to test environments and test data.

---

## FIX-002 — Maven 3.10.0 "auto-discovered prefixes" stall

**Problem:** Builds hung right after `Loaded NNNNN auto-discovered prefixes for remote
repository central (prefixes-central.txt)`. Downloads stalled intermittently.

**Root cause:** Known Maven 3.10.0 behavior — Remote Repository Filtering (RRF) fetches
repository "prefix" files too eagerly, which stalls in constrained/sandboxed networks.

**Official docs / references:**
- Remote Repository Filtering: https://maven.apache.org/resolver/remote-repository-filtering.html
- Resolver configuration: https://maven.apache.org/resolver-1.x/configuration.html
- Upstream issue: https://github.com/apache/maven/issues/13050
  ("3.10.0 might be fetching prefixes a little too eagerly")

**First attempt (OVER-CORRECTED — do not use):** disabled BOTH filters outright:
```
-Daether.remoteRepositoryFilter.prefixes=false
-Daether.remoteRepositoryFilter.groupId=false
```
After re-reading the official docs this was wrong on two counts:
1. The groupId filter is INACTIVE by default (it abstains unless you supply input
   files), so `groupId=false` did nothing.
2. Disabling `prefixes` entirely turns off the whole filtering FEATURE, which is more
   than we need. The docs provide a targeted property for exactly our symptom.

**CORRECTED FIX (official, targeted):** per the Resolver docs, disable only the
discovery/download of the prefixes file — not the filter feature:
```
-Daether.remoteRepositoryFilter.prefixes.resolvePrefixFiles=false
```
Source: https://maven.apache.org/resolver/remote-repository-filtering.html
("the user should disable prefix discovery by using
-Daether.remoteRepositoryFilter.prefixes.resolvePrefixFiles=false ... to prevent Maven
from attempting to resolve the prefixes file").

`.mvn/maven.config` is read automatically for every Maven run in this project, so the
fix travels with the repo. Verified: clean build (`mvn -B clean compile`) reaches
BUILD SUCCESS with no "Loaded prefixes" stall, and the RRF feature stays intact.

---

## FIX-003 — PowerShell swallowing Maven streaming output

**Problem:** Running `mvn ...` directly sometimes returned exit 0 with no visible
output, or appeared to hang, because Maven's progress stream didn't render well in
this PowerShell host.

**Fix (reliable command pattern on Windows):** run Maven through `cmd /c`, change into
the project dir, and redirect all output to a log file we then read:

```powershell
$env:Path += ";$env:USERPROFILE\tools\apache-maven-3.10.0\bin"
$proj = "C:\...\TestDataManager"
cmd /c "cd /d ""$proj"" && mvn -B compile > ""$env:TEMP\mvn.log"" 2>&1"
Get-Content "$env:TEMP\mvn.log" | Select-Object -Last 10
```

`-B` (batch mode) disables interactive prompts and ANSI progress spinners, which is the
recommended setting for non-interactive/CI use (Maven CLI docs).

---

## CHANGE-001 — Upgrade Jackson 2.22.3 → 3.2.3

**Reason:** You asked to use Jackson 3.x (latest major).

**Important migration facts (official):**
- Migration guide: https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md
- Release 3.0 (GA Oct 3, 2025): https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0
- **New Maven groupId:** `tools.jackson.core` (was `com.fasterxml.jackson.core`)
- **New Java package:** `tools.jackson.*` (imports change in code)
- **Requires Java 17+** (we run Java 21 ✅)

**pom.xml change:**
```xml
<dependency>
  <groupId>tools.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>3.2.3</version>
</dependency>
```
Verified: `jackson-databind-3.2.3.jar` + `jackson-core-3.2.3.jar` downloaded to the
local repo; BUILD SUCCESS.

---

## NOTE-001 — "Log4j API could not find a logging provider" (harmless)

**What you see:** When running POI code you may see:
`ERROR Log4j API could not find a logging provider.`

**Meaning:** Apache POI uses the Log4j *API* for optional internal logging, but we did
not add a Log4j *implementation* (backend) to the project. With no backend, Log4j just
prints this one notice and continues. It does **not** affect reading/writing Excel.

**Options:**
- Ignore it (fine for this project), or
- Add a backend to silence it, e.g. `org.apache.logging.log4j:log4j-core` (and
  `log4j-slf4j2-impl` if needed). We chose to keep dependencies minimal and ignore it.

Reference: Apache POI logging notes — https://poi.apache.org/components/logging.html

---

## NOTE-002 — UTF-8 BOM breaks raw `javac` (not Maven)

**What happened:** A throwaway generator compiled with raw `javac` failed with
`illegal character: '\ufeff'`. The file had a UTF-8 **BOM** (byte-order mark) at the
start, which `javac` does not accept at the top of a source file.

**Why Maven was unaffected:** the Maven compiler plugin is configured with
`project.build.sourceEncoding = UTF-8` and tolerates the BOM; only the bare `javac`
invocation tripped on it.

**Fix:** rewrote the file as UTF-8 **without BOM**:
```powershell
$enc = New-Object System.Text.UTF8Encoding($false)   # $false = no BOM
$text = [System.IO.File]::ReadAllText($f).TrimStart([char]0xFEFF)
[System.IO.File]::WriteAllText($f, $text, $enc)
```

---

## STEP-001 — Generated test_users.xlsx (12 data rows)

Created `src/main/resources/test_users.xlsx` with a one-off Apache POI generator
(compiled against the Maven dependency classpath, run once, then deleted). Columns:
email, password, firstName, lastName, role, expectedResult. Contents verified by
reading the file back: header + **12 rows** (4 admin, 8 customer; 8 pass, 4 fail).
Exceeds the 10-row requirement. All data is fake — never commit real credentials.

---

## FIX-004 — `mvn exec:java` stalls at "Scanning for projects" → run OFFLINE

**Problem:** Running the program with `mvn exec:java` hung immediately after
`Scanning for projects...`, before our code ran. This is the same Maven 3.10
remote-lookup stall seen in FIX-002, triggered here while Maven checked remote
repositories for plugin/metadata updates.

**Wrong turn (corrected):** briefly tried bypassing Maven with a raw `java -cp` run.
That defeats the purpose of using Maven. The right answer is in the Maven docs.

**Official fix — offline mode (`-o`):**
- Maven configuring guide: https://maven.apache.org/ref/4-LATEST/configuring.html
- `mvn -o` ("offline") tells Maven not to contact any remote repository; it uses only
  the local `~/.m2` cache. Since all our dependencies and the exec plugin were already
  downloaded, offline mode skips the lookups that stalled.

**Command to run the program:**
```powershell
$env:Path += ";$env:USERPROFILE\tools\apache-maven-3.10.0\bin"
mvn -o exec:java      # -o = offline; main class is set in pom.xml (exec.mainClass)
```
Result: BUILD SUCCESS, full program output printed.

**Note:** offline mode only works after the needed artifacts are cached. The first time
you add a new plugin/dependency, run once online (plain `mvn ...`) to download it, then
use `-o` afterward.

---

## FIX-005 — Correcting a bypass: helper code run with raw `javac`/`java`

**What was bypassed:** The one-off `test_users.xlsx` generator (and two throwaway
verifier classes) were compiled and run with bare `javac`/`java`, pointing a hand-built
classpath at the `.m2` jars. That sidestepped Maven, the build tool we installed to use.

**Why it is a bypass:** we standardized on Maven. Running project-adjacent Java through
raw `javac` skips Maven's dependency and classpath management.

**The Maven-native way (official):** the Exec Maven Plugin runs a main class inside
Maven with the project's dependencies as classpath — no manual classpath assembly.
- Plugin docs: https://www.mojohaus.org/exec-maven-plugin/
- `java` goal: https://www.mojohaus.org/exec-maven-plugin/java-mojo.html
- One-off main class with a chosen class:
  `mvn -o exec:java -Dexec.mainClass=the.Class`

**Resolution for this project:** `test_users.xlsx` was already generated, committed, and
verified by reading it back, so it is left as-is (regenerating adds risk, no value). The
throwaway generator/verifier files were deleted and never part of the build. Going
forward, any helper main class is run via `mvn -o exec:java -Dexec.mainClass=...`, not
raw `javac`/`java`.

---

## NOTE-003 — `cmd /c ... > log 2>&1` is a shell I/O pattern, not a Maven bypass

For the record: wrapping Maven in `cmd /c "... > log 2>&1"` (FIX-003) does NOT bypass
Maven. It is still the same `mvn` invocation; the wrapper only redirects Maven's own
output to a file because this PowerShell host does not render Maven's live progress
stream well. Maven does all the work exactly as documented.
