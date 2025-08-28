# Project TODO — Tasks Management Application (Java, MVVM, Derby, Swing + FlatLaf)

> **Scope:** Build a stand-alone desktop app in **Java 24** using **Swing** (with **FlatLaf**), strict **MVVM**, **DAO** with **embedded Apache Derby**, and the required **design patterns**.  
> Deliver **executable JAR**, **ZIP**, and **PDF** per course rules.

---

## Legend
- ☐ = To do  
- ☑ = Done  
- 🧪 = Tests  
- 📦 = Deliverable  
- 🧩 = Pattern  

---

## ✅ Completed
- ☑ **Repo initialized & pushed**
- ☑ **Task + ValidationException implemented**
- ☑ **Combinator Filters (TaskFilter + Filters)**
- ☑ **Visitor + Adapters (ByStateCount, CountByStateVisitor, PlainText/CSV exporters)**

**Re-check:**  
Requirements.md — “Mandatory Design Patterns (Combinator, Visitor)”  
Style Guide — “Comments (Javadoc)”, “Identifiers”, “Classes”, “Exception Handling”  

---

## 🔜 Remaining Work (now → submission)

### Step 4 — DAO: Embedded Derby (🧩 Singleton + DAO)
- ☐ Create `persistence/derby/EmbeddedDerbyTasksDAO` as **Singleton** (`getInstance()`, private ctor).
- ☐ Load driver `org.apache.derby.jdbc.EmbeddedDriver`; connect `jdbc:derby:derby-db;create=true`.
- ☐ Implement all `ITasksDAO` methods; wrap errors in `TasksDAOException`.
- ☐ Add `persistence/DAOProvider` returning the singleton.
- ☐ Implement `persistence/derby/DerbyConfig` (constants for DB URL, table names).
- ☐ Implement `persistence/derby/DerbyBootstrap` (initialize schema if missing):
  ```sql
  CREATE TABLE TASKS(
    ID INT PRIMARY KEY,
    TITLE VARCHAR(255) NOT NULL,
    DESCRIPTION VARCHAR(4000) NOT NULL,
    STATE VARCHAR(20) NOT NULL
  )
  ```
- ☐ Commit: `feat(dao): implement EmbeddedDerbyTasksDAO (singleton), DerbyBootstrap, DerbyConfig, and DAOProvider`

**Acceptance:** CRUD ok; DB auto-creates; no UI code in DAO  
**Tests preview:** add/get/update/delete; not-found returns null; exception paths  

**Re-check:**  
Requirements.md — “Architecture & Technologies”, “Functional Requirements”, “Interfaces to Implement”  
Style Guide — “Exception Handling”, “Separation of Concerns”, “Interfaces”  

---

### Step 5 — Mandatory Patterns
#### 5A — Combinator (☑ Done)  
#### 5B — Visitor + Adapter (☑ Done)  

---

### Step 6 — Additional Patterns (planned 6)
- ☐ **Observer** — ViewModel raises events; UI listens and updates automatically.
- ☐ **State** — Enrich `TaskState` with helpers (e.g., `canTransitionTo`) and/or behavior routing.
- ☐ **Command** — `application/commands`: `Command` (execute/undo), `AddTaskCommand`, `UpdateTaskCommand`, `DeleteTaskCommand`, `ChangeStateCommand`; `CommandManager` with undo/redo stacks.
- ☐ **Strategy** — `application/sorting`: `TaskSortStrategy` + concrete strategies (by title, state, id).
- ☐ (Optional) **Proxy** — Cache DAO queries to optimize repeated calls.
- ☐ Commit: `feat(patterns): Observer hooks, State transitions, Command + Strategy`

**Acceptance:** Undo/redo works; sort strategies change order; invalid transitions blocked  
**Tests preview:** CommandManager stacks; strategies stable; transition validation; DAO Proxy cache hit/miss  

**Re-check:**  
Requirements.md — “Additional Design Patterns”  
Style Guide — “Separation of Concerns”, “Classes”, “Identifiers”  
