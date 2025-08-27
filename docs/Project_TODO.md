
# Project TODO — Tasks Management Application (Java, MVVM, Derby, Swing + FlatLaf)

> **Scope:** Build a stand-alone desktop app in **Java 24** using **Swing** (with **FlatLaf**), strict **MVVM**, **DAO** with **embedded Apache Derby**, and the required **design patterns**. Deliver **executable JAR**, **ZIP**, and **PDF** per course rules.

---

## Legend
- ☐ = To do  ☑ = Done  🧪 = Tests  📦 = Deliverable  🧩 = Pattern

---

## Completed
- ☑ **Repo initialized & pushed**
  - Structure: `src/taskmanagement/...`, `lib/`, `.gitignore`, `README.md`, `patterns.txt`.
  - FlatLaf jar under `lib/` (to be added in IntelliJ as Library).
  - First commit & push.
  - **Re-check Requirements.md — “Architecture & Technologies”, “Submission Guidelines → IDE & JDK”; Style Guide — “Identifiers”, “Comments (Javadoc)”.**

---

## Remaining Work (now → submission)

### Step 3 — Model: `Task` class + Validation
- ☐ Implement `taskmanagement.domain.Task` with fields: `id`, `title`, `description`, `state: TaskState`.
- ☐ Setters with validation (non-null; title not blank & trimmed; sensible length limits). Constructors call setters.
- ☐ Implement `equals`, `hashCode`, `toString`; add **Javadoc** to all non-private members.
- ☐ Commit: `feat(model): add Task with validation and overrides`
- 🔍 Acceptance: compiles; validations enforced; equals/hashCode consistent.
- 🧪 Tests preview: invalid titles, null state, equality & hashCode.
- **Re-check Requirements.md — “The Application”, “Functional Requirements”, “Interfaces to Implement”; Style Guide — “Classes”, “Identifiers”, “Comments (Javadoc)”, “Exception Handling”.**

---

### Step 4 — DAO: Embedded Derby (🧩 Singleton + DAO)
- ☐ Create `persistence/derby/EmbeddedDerbyTasksDAO` as **Singleton** (`getInstance()`; private ctor).
- ☐ Load driver `org.apache.derby.jdbc.EmbeddedDriver`; connect `jdbc:derby:derby-db;create=true`.
- ☐ Initialize schema if missing:
  ```sql
  CREATE TABLE TASKS(
    ID INT PRIMARY KEY,
    TITLE VARCHAR(255) NOT NULL,
    DESCRIPTION VARCHAR(4000) NOT NULL,
    STATE VARCHAR(20) NOT NULL
  )
  ```
- ☐ Implement all `ITasksDAO` methods; wrap errors in `TasksDAOException`.
- ☐ Add `persistence/DAOProvider` returning the singleton.
- ☐ Commit: `feat(dao): implement EmbeddedDerbyTasksDAO (singleton) and DAOProvider`
- 🔍 Acceptance: CRUD ok; DB auto-creates; no UI code in DAO.
- 🧪 Tests preview: add/get/update/delete; not-found returns null; exception paths.
- **Re-check Requirements.md — “Architecture & Technologies”, “Functional Requirements”, “Interfaces to Implement”; Style Guide — “Exception Handling”, “Separation of Concerns”, “Interfaces”.**

---

### Step 5 — Mandatory Patterns

#### 5A — Combinator Filters (🧩 Combinator)
- ☐ `domain/filter/TaskFilter` (functional interface: `boolean test(ITask t)`).
- ☐ `domain/filter/Filters`: `byState(...)`, `titleContains(...)`, `descriptionContains(...)`, plus `and(...)`, `or(...)`, `not(...)`.
- ☐ Commit: `feat(filter): add composable TaskFilter with AND/OR/NOT`
- 🔍 Acceptance: Composed filters behave predictably.
- 🧪 Tests preview: AND/OR/NOT combinations; case sensitivity rules documented.
- **Re-check Requirements.md — “Mandatory Design Patterns (Combinator)”; Style Guide — “Interfaces”, “Comments (Javadoc)”.**

#### 5B — Visitor with Records & Pattern Matching (🧩 Visitor + 🧩 Adapter)
- ☐ Record(s): `domain/visitor/reports/ByStateCount(int todo, int inProgress, int completed)`.
- ☐ Concrete visitor that traverses task lists, producing the record(s).
- ☐ **Adapter** exporters: `adapters/ReportExporter` + `ByStateCsvExporter`, `ByStatePlainTextExporter`.
- ☐ Commit: `feat(visitor): add record-based reports and exporters (adapter)`
- 🔍 Acceptance: counts correct; exporters format as expected.
- 🧪 Tests preview: visitor accumulation; exporter formatting edge cases.
- **Re-check Requirements.md — “Mandatory Design Patterns (Visitor)”; Style Guide — “Interfaces”, “Classes”, “Comments (Javadoc)”.**

---

### Step 6 — Additional Patterns (planned 6)
- ☐ **Observer** — ViewModel raises events; UI listens and updates automatically.
- ☐ **State** — Enrich `TaskState` with helpers (e.g., `canTransitionTo`) and/or behavior routing.
- ☐ **Command** — `application/commands`: `Command` (execute/undo), `AddTaskCommand`, `UpdateTaskCommand`, `DeleteTaskCommand`, `ChangeStateCommand`; `CommandManager` with undo/redo stacks.
- ☐ **Strategy** — `application/sorting`: `TaskSortStrategy` + concrete strategies (by title, state, id).
- ☐ Commit: `feat(patterns): Observer hooks, State transitions, Command + Strategy`
- 🔍 Acceptance: Undo/redo works; sort strategies change order; invalid transitions blocked.
- 🧪 Tests preview: CommandManager stacks; strategies stable; transition validation.
- **Re-check Requirements.md — “Additional Design Patterns”; Style Guide — “Separation of Concerns”, “Classes”, “Identifiers”.**

---

### Step 7 — ViewModel Layer (MVVM core)
- ☐ Implement `application/TaskViewModel` exposing:
  - Observable task list & events: `tasksChanged`, `selectionChanged`.
  - API: `addTask`, `updateTask`, `deleteTask`, `setFilter`, `applySort`, `generateByStateReport`, `undo`, `redo`.
  - Internals: use DAO + CommandManager; raise Observer events.
- ☐ Commit: `feat(vm): add TaskViewModel with observable API and command wiring`
- 🔍 Acceptance: No Swing imports; event-driven; testable.
- 🧪 Tests preview: event firing; filter/sort integration.
- **Re-check Requirements.md — “Architecture & Technologies (MVVM)”; Style Guide — “Separation of Concerns”, “Interfaces”, “Comments (Javadoc)”.**

---

### Step 8 — Swing View (UI on EDT, using FlatLaf)
- ☐ `app/App.java` main (EDT + LAF):
  ```java
  SwingUtilities.invokeLater(() -> {
      try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); }
      catch (Exception ignored) {}
      // new MainFrame(viewModel).setVisible(true);
  });
  ```
- ☐ Build screens: main frame, task table, editor panel/dialog, filters/search, sort dropdown, report display.
- ☐ Bind to ViewModel (Observer): refresh on `tasksChanged`; actions call VM methods.
- ☐ Shortcuts: Undo (Ctrl+Z), Redo (Ctrl+Y).
- ☐ Commit: `feat(ui): Swing MVVM UI with FlatLaf, filtering, sorting, reporting`
- 🔍 Acceptance: No DAO in UI; responsive updates; EDT only.
- 🧪 Manual tests: add/edit/delete; mark states; filter/sort; export; undo/redo.
- **Re-check Requirements.md — “The Application”, “Functional Requirements”; Style Guide — “User Interface (EDT)”, “Separation of Concerns”, “Identifiers”.**

---

### Step 9 — Unit Tests
- ☐ Add JUnit tests for: `Task` validation; `Filters`; Visitor report; `CommandManager`; DAO CRUD (temp DB).
- ☐ Commit: `test: add unit tests for model, filters, visitor, commands, dao`
- 🔍 Acceptance: tests pass locally; deterministic; UI-free.
- **Re-check Requirements.md — “Non-Functional Requirements (Unit tests)”; Style Guide — “Exception Handling”, “Classes”.**

---

### Step 10 — Packaging: Executable JAR
- ☐ IntelliJ → **Build Artifacts → JAR (From modules with dependencies)** → Main class `taskmanagement.app.App`.
- ☐ Ensure FlatLaf & Derby are included by artifact settings.
- ☐ Validate: `java -jar firstname_lastname.jar` (DB creates, UI launches).
- ☐ Commit (if stored): `build: artifact config for executable JAR`
- 📦 Deliverable: `firstname_lastname.jar`
- **Re-check Requirements.md — “Submission Guidelines → Files to Submit (JAR)”; Style Guide — “Separation of Concerns”.**

---

### Step 11 — Submission Docs
- ☐ Record **YouTube (unlisted)** demo (~60s+): explain Combinator, Visitor, and chosen patterns (Observer, State, Command, Strategy, Singleton, Adapter).
- ☐ Create **PDF**:
  - First page: team manager + full team (name, ID, mobile, email).
  - Clickable YouTube link.
  - ≤50-word explanation per pattern with class names.
  - All code files (left-aligned; no broken lines).
- ☐ Export **ZIP** (IntelliJ → Export → Project to Zip).
- 📦 Deliverables: `firstname_lastname.pdf`, `firstname_lastname.zip`, `firstname_lastname.jar`
- ☐ Commit: `docs: add PDF draft and pattern summaries`
- **Re-check Requirements.md — “Submission Guidelines (Video, Files, PDF rules)”; Style Guide — “Comments (Javadoc)”.**

---

### Step 12 — Final Checklist & Polish
- ☐ Walk **Requirements.md** end-to-end: features, MVVM, DAO, Derby, patterns (2 mandatory + ≥4 additional), tests, JAR, submission rules.
- ☐ Walk **Java_Style_and_doc_guideline.md** end-to-end: Javadoc coverage, identifiers, validation in setters, equals/hashCode, UI on EDT.
- ☐ Final push; GitHub review.
- **Re-check Requirements.md — “Entire document”; Style Guide — “Entire document”.**

---

## Commit Message Templates
- Model: `feat(model): add Task with validation and overrides`
- DAO: `feat(dao): implement EmbeddedDerbyTasksDAO (singleton) and DAOProvider`
- Filters: `feat(filter): add composable TaskFilter with AND/OR/NOT`
- Visitor/Adapter: `feat(visitor): add record-based reports and exporters (adapter)`
- Patterns Set: `feat(patterns): Observer hooks, State transitions, Command + Strategy`
- ViewModel: `feat(vm): add TaskViewModel with observable API and command wiring`
- UI: `feat(ui): Swing MVVM UI with FlatLaf, filtering, sorting, reporting`
- Tests: `test: add unit tests for model, filters, visitor, commands, dao`
- Build/JAR: `build: artifact config for executable JAR`
- Docs: `docs: add PDF draft and pattern summaries`

---

## Quick References
- **Derby JDBC URL**: `jdbc:derby:derby-db;create=true`
- **FlatLaf setup (EDT)**:
  ```java
  SwingUtilities.invokeLater(() -> {
      try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); }
      catch (Exception ignored) {}
      // new MainFrame(viewModel).setVisible(true);
  });
  ```

---

> At the end of **every step**, remember: **Re-check Requirements.md section X and Style Guide section Y for compliance.**
