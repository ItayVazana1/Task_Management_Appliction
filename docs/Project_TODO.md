# Project TODO — Tasks Management Application (Java, MVVM, Derby, Swing + FlatLaf)

> Scope: Build a stand-alone desktop app in Java 24 using Swing (with FlatLaf), strict MVVM, DAO with embedded Apache Derby, and the required design patterns.
> Deliver executable JAR, ZIP, and PDF per course rules.

---

Legend
- ☐ = To do
- ☑ = Done
- 🧪 = Tests
- 📦 = Deliverable
- 🧩 = Pattern

---

✅ Completed
- ☑ Repo initialized & pushed
- ☑ Task + ValidationException implemented
- ☑ Combinator Filters (TaskFilter + Filters)
- ☑ Visitor + Adapters (ByStateCount, CountByStateVisitor, PlainText/CSV exporters)
- ☑ DAO layer: EmbeddedDerbyTasksDAO (Singleton), DerbyBootstrap, DerbyConfig, DAOProvider
- ☑ FullStackSmokeTest passed (CRUD, filters, visitor, exporters)

Re-check:
Requirements.md — “Mandatory Design Patterns (Combinator, Visitor)”, “Architecture & Technologies”, “Interfaces to Implement”
Style Guide — “Comments (Javadoc)”, “Identifiers”, “Classes”, “Exception Handling”, “Separation of Concerns”

---

🔜 Remaining Work (now → submission)

### Step 6 — Additional Patterns (planned 4)
- ☐ Observer — ViewModel raises events; UI listens and updates automatically.
- ☐ State — Enrich TaskState with helpers (e.g., canTransitionTo) and/or behavior routing.
- ☐ Command — application/commands: Command (execute/undo), AddTaskCommand, UpdateTaskCommand, DeleteTaskCommand, ChangeStateCommand; CommandManager with undo/redo stacks.
- ☐ Strategy — application/sorting: TaskSortStrategy + concrete strategies (by title, state, id).
- ☐ Commit: feat(patterns): Observer hooks, State transitions, Command + Strategy

Acceptance: Undo/redo works; sort strategies change order; invalid transitions blocked
Tests preview: CommandManager stacks; strategies stable; transition validation

Re-check:
Requirements.md — “Additional Design Patterns”
Style Guide — “Separation of Concerns”, “Classes”, “Identifiers”

---

### Step 7 — ViewModel
- ☐ Implement TasksViewModel bridging DAO ↔ UI
- ☐ Expose observable properties/lists for tasks, selection, filters
- ☐ Wire Command + Strategy + Observer here
- ☐ Commit: feat(viewmodel): TasksViewModel with bindings and events

---

### Step 8 — Swing View (UI)
- ☐ Build MainFrame with panels (TaskListPanel, FiltersPanel, TaskEditorDialog)
- ☐ Bind to ViewModel only (not Model/DAO directly)
- ☐ Ensure UI auto-updates (Observer)
- ☐ Commit: feat(ui): Swing GUI with FlatLaf and MVVM binding

---

### Step 9 — Unit Tests
- ☐ Add JUnit tests for DAO, Filters, Visitor reports, Commands, Strategies
- ☐ Commit: test: add unit tests for DAO, filters, visitor, patterns

---

### Step 10 — Build Deliverables
- ☐ Configure IntelliJ artifacts → executable JAR
- ☐ Export project ZIP
- ☐ Generate PDF (team details, YouTube link, pattern explanations, all code)
- ☐ File naming: firstname_lastname.jar / .zip / .pdf

---

📦 Final Submission
- Executable JAR launches with DB
- ZIP exported from IntelliJ
- PDF with all code, pattern explanations, video link
- Team manager submits only
