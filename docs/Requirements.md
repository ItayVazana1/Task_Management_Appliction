# 📌 Tasks Management Application  
### 🎓 Final Project in Design Patterns  

---

> ⚠️ **Important Note**  
> During the meetings until the end of the semester, minor textual clarifications may be introduced into this document.  
> **The requirements themselves will not change.**  
> Make sure your project complies with the latest version of this document before the final submission.  

💡 For clarifications, the **fastest way** is to post each question as a separate post in the course forum.  
Follow the forum updates to ensure that your interpretation remains correct.  

---

## 📖 Introduction  

In this final project, you are required to design and develop a **stand-alone Java application** with a **Swing-based graphical user interface (GUI)**.  

- 🏗️ The application must follow the **MVVM (Model–View–ViewModel)** architecture.  
- 🗄️ The **Model** must implement the **DAO (Data Access Object)** design pattern.  
- 💾 **Apache Derby (embedded mode)** will be used for persistent data storage.  

This project is your opportunity to apply the theoretical knowledge acquired in class, demonstrate proficiency in **design pattern implementation**, and deliver a **fully functional desktop application**.  

---

## 📝 The Application  

The application is a **Tasks Management Application**, with persistent storage in an embedded DerbyDB database.  
The project must be delivered as an **executable JAR** file.  

### ✅ Core Features  

| Feature | Description |
|---------|-------------|
| ➕ Add/Edit/Delete | Manage tasks lifecycle |
| 🔖 Task States | ToDo, In Progress, Completed |
| 🔍 Filters | Flexible filtering using **Combinator logic** |
| 📊 Reports | Generated via **Visitor** with Records & Pattern Matching |
| 🎨 GUI | Interactive Swing GUI with **MVVM** separation |  

---

## 🏗️ Architecture & Technologies  

| Component | Technology |
|-----------|------------|
| Programming Language | Java 24+ |
| UI Framework | Swing |
| Database | Apache Derby (embedded) |
| Architecture | MVVM (Model–View–ViewModel) |  

---

## ✍️ Code Style  

All Java code must comply with the style guide:  
👉 [Java Points Style Guide](https://tinyurl.com/javapoints)  

---

## 🎯 Mandatory Design Patterns  

- **Combinator** → Flexible task filtering/searching (combine filters like *due date* AND *state* OR *title*).  
- **Visitor (with Records & Pattern Matching)** → Report generation/export using Java records and visitor pattern.  

---

## ➕ Additional Design Patterns  

You must implement at least **four** patterns from the following list:  

- 🛡️ **Proxy** – Caching queries from the database.  
- 🔒 **Singleton** – DAO implementation.  
- 🔌 **Adapter** – For external reporting/export modules.  
- 🎀 **Decorator** – Add dynamic features (priority, reminders).  
- 👀 **Observer** – UI auto-updates on model change.  
- 🌳 **Composite** – Task/subtask hierarchy.  
- 🪶 **Flyweight** – Memory optimization for repeated attributes.  
- 🎯 **Strategy** – Sorting & prioritization (different strategies).  
- 🔄 **State** – Lifecycle of a task (*ToDo*, *InProgress*, *Completed*).  
- ⏪ **Command** – Undo/redo operations (add, delete, update).  

---

## 🧩 Interfaces to Implement  

```java
public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    TaskState getState();
    void accept(TaskVisitor visitor);
}
```

```java
public interface ITasksDAO {
    ITask[] getTasks() throws TasksDAOException;
    ITask getTask(int id) throws TasksDAOException;
    void addTask(ITask task) throws TasksDAOException;
    void updateTask(ITask task) throws TasksDAOException;
    void deleteTasks() throws TasksDAOException;
    void deleteTask(int id) throws TasksDAOException;
}
```

---

## ⚙️ Functional Requirements  

- ➕ Add, ✏️ edit, 🗑️ delete, and 📋 list tasks.  
- 🔄 Each task must follow the **State pattern**.  
- 👀 **Observer** ensures UI auto-updates on changes.  
- 📊 Reports via **Visitor** with records & pattern matching.  
- 🔍 Filters via **Combinator**.  
- 💾 Full persistence in embedded **DerbyDB**.  

---

## 📐 Non-Functional Requirements  

- ✅ Strict **MVVM** compliance.  
- ♻️ Modular, reusable, well-documented code.  
- 🎯 At least **4 additional design patterns** demonstrated.  
- 🧪 Unit tests with **JUnit** for critical components.  

---

## 📤 Submission Guidelines  

### 1. IDE & JDK  
- Develop using **IntelliJ IDEA** (Ultimate or Community).  
- Use **JDK 24**.  

### 2. Video Demonstration  
- 🎥 Create a **short video (≈60s)** demonstrating the project.  
- Upload to **YouTube (unlisted)**.  
- Must explain in your **voice** (not only text):  
  - **Combinator**  
  - **Visitor**  
  - The 4 chosen patterns  

### 3. Files to Submit (via Moodle)  
- 📦 **ZIP** (IntelliJ Export → Project to ZIP).  
- ⚙️ **Executable JAR**.  
- 📑 **PDF** with all project code.  
- File naming format:  

```
firstname_lastname.zip
firstname_lastname.pdf
firstname_lastname.jar
```

⚠️ Rules:  
- Only English letters, numbers, and underscores.  
- No spaces, no special characters.  

### 4. PDF File Requirements  
- Include **all code files** (no broken lines, left-aligned).  
- First page must contain:  
  - 👤 Team manager’s **first + last name**.  
  - 👥 Full details of all team members (name, ID, mobile, email).  
  - 🔗 Clickable **YouTube video link**.  
  - 📝 Short (≤50 words) explanation for each **pattern** implemented (with class names).  

### 5. Submission Responsibility  
- 📌 Only the **team manager** submits.  
- ❌ No single-student submissions.  
- ⏳ No extensions (except official cases).  
- ⚠️ Treat the deadline as **30 minutes earlier** than listed (server time differences).  

### 6. Deadline  
- 📅 Published on the **Moodle course board**.  

---

✅ That’s the full set of requirements and submission rules.  
🚀 Good luck with your implementation!  
