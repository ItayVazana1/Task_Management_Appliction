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
- ☑ FullStackSmokeTest passed (CRUD, filters, visitor, exporters, State, Command, Strategy, Observer scaffolding)

Re-check:  
Requirements.md — “Mandatory Design Patterns (Combinator, Visitor)”, “Architecture & Technologies”, “Interfaces to Implement”  
Style Guide — “Comments (Javadoc)”, “Identifiers”, “Classes”, “Exception Handling”, “Separation of Concerns”

---

🔜 Remaining Work (now → submission)

## Step 6 — Additional Patterns (finalize)
- ☑ Observer — ViewModel raises events; UI listens and updates automatically.
- ☑ State — TaskState lifecycle with transitions enforced.
- ☑ Command — Add/Update/Delete/MarkState commands with CommandStack (undo/redo).
- ☑ Strategy — SortStrategy (by title, state) verified in tests.
- ☐ Commit: feat(patterns): finalize Observer-UI hooks + verify undo/redo & sorting stability

Acceptance: Undo/redo works; sort strategies change order; invalid transitions blocked.  
Tests: CommandStack push/pop, redo cleanup, strategy ordering stable.

Re-check:  
Requirements.md — “Additional Design Patterns”  
Style Guide — “Separation of Concerns”, “Classes”, “Identifiers”

---

## Step 7 — ViewModel
- ☐ Complete TasksViewModel bridging DAO ↔ UI
- ☐ Expose observable lists/properties for tasks, selection, filters
- ☐ Integrate Command + Strategy + Observer firing
- ☐ Commit: feat(viewmodel): TasksViewModel full bindings and events

---

## Step 8 — Swing View (UI)
- ☐ MainFrame: menu (New, Edit, Delete, Undo, Redo, Report), strategy chooser, FlatLaf setup
- ☐ TaskListPanel: JTable bound to ViewModel.ObservableList
- ☐ FiltersPanel: controls for Combinator filters (AND/OR)
- ☐ TaskEditorDialog: form for add/edit with ValidationException error display
- ☐ Ensure updates occur on EDT only
- ☐ Commit: feat(ui): Swing GUI with MVVM binding

---

## Step 9 — Unit Tests
- ☐ DAO: edge cases (non-existent id, empty DB)
- ☐ Filters: AND/OR edge cases, empty result
- ☐ Visitor: report correctness, UTF-8 CSV/Plain
- ☐ Commands: undo/redo chains, redo cleanup
- ☐ Strategies: stable ordering
- ☐ Observer: events firing reflected in UI lists
- ☐ Commit: test: add unit tests for DAO, filters, visitor, patterns

---

## Step 10 — Build Deliverables
- ☐ Configure IntelliJ artifacts → executable JAR (main class set)
- ☐ Export project ZIP from IntelliJ
- ☐ Generate PDF: team details, YouTube link, ≤50 words explanations per pattern (with class names), all code left-aligned
- ☐ File naming: firstname_lastname.jar / .zip / .pdf

---

📦 Final Submission
- Executable JAR launches with DB
- ZIP exported from IntelliJ
- PDF complete with code, pattern explanations, video link
- Team manager submits only
