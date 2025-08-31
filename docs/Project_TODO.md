# Project TODO — Tasks Management Application (Java, MVVM, Derby, Swing)

> Scope: Build a stand-alone desktop app in Java 24 using Swing, strict MVVM, DAO with embedded Apache Derby, and the required design patterns.
> Deliver executable JAR, ZIP, and PDF per course rules.

Legend
- ☐ = To do
- ☑ = Done
- 🧪 = Tests
- 📦 = Deliverable
- 🧩 = Pattern

---

✅ Completed
- ☑ Repo initialized & pushed
- ☑ Domain: ITask, Task, TaskState (+ validation), project exceptions
- ☑ Filters (Combinator): ITaskFilter + Filters (AND/OR)
- ☑ Visitor: TaskVisitor + reports (ByStateCount) + exporters (Plain/CSV)
- ☑ DAO: EmbeddedDerbyTasksDAO + DerbyBootstrap + DerbyConfig + DAOProvider
- ☑ Patterns (mandatory + four):
    - 🧩 Combinator, 🧩 Visitor
    - 🧩 Observer (VM notifies; UI subscribes)
    - 🧩 State (Task lifecycle)
    - 🧩 Command (Add/Update/Delete/Mark + CommandStack: undo/redo)
    - 🧩 Strategy (Sort by title/state)
- ☑ Step 7 — ViewModel:
    - Exposed observable lists/properties (tasks, selection, filters)
    - Integrated Strategy + Command + Observer events
    - Bridged DAO ↔ UI without UI code
- ☑ Initial UI structure:
    - MainFrame, TaskListPanel, FiltersPanel, TaskEditorDialog, AboutDialog
    - Central UITheme for colors/typography/helpers

---

🚧 In Progress — Step 8: Swing View (Re-design & polish)
- ☐ MainFrame:
    - Wire menu/actions: New, Edit, Delete, Undo, Redo, Report, Exit
    - Hook strategy chooser (Strategy) + toolbar shortcuts
    - Ensure EDT usage for all UI updates
- ☐ TaskListPanel:
    - Bind to ViewModel observable list; stable selection after refresh
    - Double-click → Edit; Delete key → delete; Enter → edit
    - Empty-state message when list is empty
- ☐ FiltersPanel:
    - Title contains + state filter; AND/OR combinators (Combinator)
    - “Clear filters” and live apply on change
- ☐ TaskEditorDialog:
    - Add/Edit flows; show ValidationException messages inline
    - OK/Cancel keyboard shortcuts; default button; focus handling
- ☐ Reporting (Visitor):
    - Trigger ByState report → choose exporter (Plain/CSV) → save dialog
- ☐ Theming & layout:
    - Apply UITheme consistently (paddings, gaps, titles, rounded borders)
    - Table renderers for state pills; consistent spacing; icons (optional)
- ☐ QA pass:
    - Add/Delete/Edit reflect immediately; no flicker; no EDT violations

---

🧪 Step 9 — Unit Tests
- ☐ DAO edge cases (missing id, empty DB)
- ☐ Filters AND/OR combinations + empty results
- ☐ Visitor outputs (counts per state; CSV/Plain UTF-8)
- ☐ Command: multi-step undo/redo, redo-chain invalidation after new command
- ☐ Strategy: stable ordering; switching strategies reflects in UI list
- ☐ Observer: events propagate; UI list updates once per change (no duplicates)

---

📦 Step 10 — Build Deliverables
- ☐ IntelliJ Artifacts → executable JAR (main class set, Derby on classpath)
- ☐ Export project ZIP (File → Export → Project to Zip)
- ☐ Single PDF:
    - Team details
    - Unlisted YouTube link (demo)
    - ≤50-word explanation per pattern + class names
    - All code left-aligned (no broken lines)
- ☐ File names: firstname_lastname.jar / .zip / .pdf

---

🔖 Nice-to-have (time-boxed)
- ☐ Keyboard shortcuts (Ctrl+N/E/D/Z/Y, Ctrl+F)
- ☐ Status bar with counts (total / per state)
- ☐ Minimal icons for actions (no external libs)

---

Next commits
- feat(ui): finalize MainFrame actions + strategy switcher + EDT guards
- feat(ui): bind TaskListPanel selection & shortcuts; empty-state
- feat(ui): FiltersPanel AND/OR wiring; clear filters
- feat(ui): TaskEditorDialog validation UX
- feat(report): Visitor report export flow (Plain/CSV)
- chore: polish UITheme usage across panels

