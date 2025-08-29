# Tasks Management Application
## Final Project in Design Patterns

---

### Important Note
During the meetings until the end of the semester, minor textual clarifications may be introduced into this document.  
The requirements themselves will not change.  
Make sure your project complies with the latest version of this document by the end of this semester.

For clarifications, the fastest way is to post each question as a separate post in our course’s forum.  
Follow the forum updates to ensure that your interpretation is the accurate one.

---

## Introduction
In this final project, you are required to design and develop a stand-alone Java application with a graphical user interface (GUI) built using Swing.  
The application will follow the Model-View-ViewModel (MVVM) architectural pattern, the model will include an implementation of the Data Access Object (DAO) design pattern, and it will incorporate an embedded Apache Derby database for persistent data storage.

The project will serve as an opportunity to apply the theoretical knowledge acquired during the course, demonstrate proficiency in design pattern implementation, and deliver a fully functioning desktop application.

---

## The Application
The application to be developed is a Tasks Management Application that allows users to manage tasks with persistence in an embedded DerbyDB database.  
The application will be available as an executable JAR file.

### Core Features
- Creating new tasks, editing existing ones, and deleting tasks.  
- Marking tasks with states (e.g., “To Do”, “In Progress”, “Completed”).  
- Applying filters using Combinator logic.  
- Generating a report using the Visitor pattern, implemented with Records and Pattern Matching.  
- Interactive and responsive Swing GUI with MVVM separation.  

---

## Architecture & Technologies
- Programming Language: Java 24+  
- UI Framework: Swing  
- Database: Apache Derby (embedded mode)  
- Architecture: MVVM (Model–View–ViewModel)  

---

## Code Style
The code in Java should follow the style guide:  
https://tinyurl.com/javapoints

---

## Mandatory Design Patterns
The following patterns must be implemented explicitly in the application:  
- Combinator – Implement flexible task filtering and searching (e.g., combining filters for “by due date” AND “by state” OR “by title”).  
- Visitor (with Record & Pattern Matching) – Implement report generation/export functionality using Java records to model tasks and apply pattern matching in the visitor implementation.  

---

## Additional Design Patterns
Students must select and correctly implement at least four patterns from the following list:  
- Proxy – For caching queries from the database.  
- Singleton – For the Data Access Object implementation.  
- Adapter – To adapt external reporting/export modules to application interfaces.  
- Decorator – To dynamically enhance tasks (e.g., add priority, deadline reminders).  
- Observer – For UI updates when the model changes.  
- Composite – For hierarchical task subtasks, if desired.  
- Flyweight – To optimize memory usage for repeated task attributes (statuses).  
- Strategy – For sorting and task prioritization (different sorting strategies).  
- State – To represent the lifecycle of a task (ToDo, InProgress, Completed).  
- Command – To implement undo/redo of task operations (add, delete, update).  

---

## Interfaces to Implement
public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    TaskState getState();
    void accept(TaskVisitor visitor);
}

public interface ITasksDAO {
    ITask[] getTasks() throws TasksDAOException;
    ITask getTask(int id) throws TasksDAOException;
    void addTask(ITask task) throws TasksDAOException;
    void updateTask(ITask task) throws TasksDAOException;
    void deleteTasks() throws TasksDAOException;
    void deleteTask(int id) throws TasksDAOException;
}

---

## Functional Requirements
- The system must allow adding, editing, deleting, and listing tasks.  
- Each task must have a state (State pattern).  
- The UI must update automatically when tasks change (Observer pattern).  
- Reports must be generated via a Visitor implemented with records and pattern matching.  
- Filters must be implemented via Combinator.  
- Database operations must be fully embedded in DerbyDB.  

---

## Non-Functional Requirements
- The system must follow MVVM strictly.  
- The code must be modular, reusable, and well-documented.  
- At least four of the patterns listed must be demonstrated.  
- Unit tests should be written for critical components (JUnit).  

---

## Submission Guidelines
0. You should develop the project with the IntelliJ IDE. It can be the ultimate version or the community one. Both of them are OK. You should use JDK 24.

1. You should create a short video (≈60s, or longer if needed) that shows how the project runs.  
   You should upload that video to YouTube and make sure you upload it as an unlisted video.  
   The video should include your explanation for implementing Combinator, Visitor, and the four patterns you chose to implement.  
   The explanation should be in your voice (not only text).  

2. You should pack the entire project into a ZIP file (File → Export → Project to Zip file in IntelliJ).  
   Together with the executable JAR file and the PDF file, you should upload these three files to the submission box on Moodle.  
   The names of the ZIP, the PDF, and the JAR files should be:  
   firstname_lastname.zip  
   firstname_lastname.pdf  
   firstname_lastname.jar  
   File names must not include spaces, non-English letters, or special characters. Use underscores for spaces.  

3. You should create a single PDF file and copy all code files coded by you into it.  
   Make sure that lines are not broken, alignment is to the left, and the PDF is properly organized to allow code review.  

4. The PDF file should include (at the beginning):  
   - The first name and the last name of the development team manager.  
   - First name + Last name + ID + Mobile Number + Email Address of each team member.  
   - Clickable link to the video created.  
   - Short explanation (≤50 words) for each implemented design pattern (with class names).  

5. Only the team manager should submit the three files (ZIP + JAR + PDF).  
   Other team members should not submit.  
   No late submissions will be accepted.  
   Submissions of projects developed by a single student will not be accepted.  
   Treat the deadline as 30 minutes earlier than listed (server time differences).  

6. Deadline:  
   15.09.2025 at 15:00  

---

## Questions & Answers
Q: Can Observer be implemented using ActionListener?  
A: No. The Observer must be implemented to update the UI as a result of changes in the Model.  
It cannot be implemented using ActionListener interfaces.  

---

## Document Modifications
Minor clarifications may be added during the semester, but the requirements themselves will not change.
