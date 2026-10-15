# Branching Strategy

How this repository organizes code in Git, and how that differs from deployment
environments. Written as a reference you can explain in an interview.

---

## 1. Branches vs. Environments — do not confuse them

Two separate pipelines that beginners often mix up:

| Concept | What it is | Examples |
|---|---|---|
| **Branch strategy** | How *code* is organized in Git | `main`, `feature/...` |
| **Environments** | Where running software is *deployed* | dev, test, uat, prod |

`dev / test / uat / prod` are **environments**, not Git branches. A common mistake
is creating one long-lived Git branch per environment. Modern practice ties an
environment to a **deployment** (and to CI/CD), not to a permanent branch.

---

## 2. This repo uses GitHub Flow

GitHub Flow is the simple, modern workflow most teams use today.

```
main  ──●───────●────────●──────────●──────>   default + protected, always releasable
         \      ↑         \         ↑
          ●─────●          ●────────●
       feature/add-readme   feature/add-csv-provider
       (short-lived)        (short-lived)
```

**Rules**
1. `main` is the default branch and is protected — no direct pushes; always working.
2. Every change starts on a short-lived branch: `feature/...`, `fix/...`, `docs/...`.
3. Open a Pull Request → CI runs the build/tests → review → merge to `main`.
4. Delete the feature branch after merge.
5. Tag releases on `main` (e.g. `v1.0.0`) when you need versioned releases.

**Why not Git Flow?** The older Git Flow (long-lived `develop`, plus `release/*`
and `hotfix/*` branches) is heavier and now largely legacy. For CI/CD and most
teams, GitHub Flow is the standard.

---

## 3. Does a test-automation project need dev → test → uat → prod?

**No.** That environment chain exists to protect the *end users* of a
customer-facing application. A **test-automation utility has no such end users**,
so it does not get its own uat/prod deployment chain.

Instead, test automation has its own lifecycle shaped by CI/CD:

```
Application under test:   dev → test → uat → prod      (protects users)
Test automation code:     branch → PR → CI runs tests → merge → runs in CI/CD
```

The test code is **not deployed to prod** — it is **executed in CI/CD** against the
application in each environment, and it gates whether the app may promote to the
next environment (e.g. tests must pass in `test` before the app goes to `uat`).

Example pipeline (from a related project):
```
GitHub Actions → test runner → JUnit XML → results pushed to TestRail
```
The tests ran in CI and reported results. No dev/test/uat/prod branches for the
test code itself.

---

## 4. Branch naming used here

| Prefix | Use | Example |
|---|---|---|
| `feature/` | New functionality | `feature/add-readme` |
| `fix/` | Bug fix | `fix/null-cell-handling` |
| `docs/` | Documentation only | `docs/update-structure` |

Keep branches short-lived: branch, change, PR, merge, delete.
