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

**Fix:** Added project-level `.mvn/maven.config` to disable the prefix/groupId filters:

```
-Daether.remoteRepositoryFilter.prefixes=false
-Daether.remoteRepositoryFilter.groupId=false
```

`.mvn/maven.config` is read automatically for every Maven run in this project, so the
fix travels with the repo. After this, dependency downloads completed and builds
reached BUILD SUCCESS.

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
