# 📑 Design Patterns Summary

## 🔹 Combinator

**Classes:** `ITaskFilter`, `AndFilter`, `OrFilter`, `TitleFilter`, `StateFilter`  
**Explanation:**  
We used the **Combinator** pattern to build flexible task filters. Each filter implements `ITaskFilter`, and filters can be combined with `AndFilter` or `OrFilter`, enabling advanced queries such as “tasks with title containing X AND state = Completed”.

---

## 🔹 Visitor (Records & Pattern Matching)

**Classes:** `TaskVisitor`, `ToDoTaskRec`, `InProgressTaskRec`, `CompletedTaskRec`, `ByStateCount`  
**Explanation:**  
The **Visitor** pattern with **Java records + pattern matching** enables generating reports without modifying `Task`. Visitors can process tasks differently (e.g., counting tasks by state) while tasks simply accept visitors.

---

## 🔹 Observer

**Classes:** `Property<T>`, `TasksViewModel`  
**Explanation:**  
The **Observer** pattern ensures automatic UI updates. The `Property<T>` class notifies observers when values change, and the `TasksViewModel` uses it to keep Swing components synchronized with the model layer in real time.

---

## 🔹 State

**Classes:** `TaskState` (enum), `Task` (`transitionTo`, `advanceState`)  
**Explanation:**  
The **State** pattern models a task’s lifecycle: **ToDo → InProgress → Completed**. Transitions are validated in `Task`, ensuring that only legal state changes occur, encapsulating behavior with clean enum-driven logic.

---

## 🔹 Strategy

**Classes:** `SortStrategy`, `SortById`, `SortByTitle`, `SortByState`  
**Explanation:**  
The **Strategy** pattern provides multiple interchangeable sorting behaviors. The ViewModel selects the appropriate strategy (by ID, title, or state) to sort tasks dynamically without changing the task model.

---

## 🔹 Command

**Classes:** `Command`, `AddTaskCommand`, `UpdateTaskCommand`, `DeleteTaskCommand`, `AdvanceStateCommand`  
**Explanation:**  
The **Command** pattern encapsulates user actions (add, edit, delete, advance state) into objects. This allows us to implement **undo/redo** functionality consistently and decouple the UI from direct model operations.

---
