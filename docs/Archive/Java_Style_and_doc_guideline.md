# ☕ Java Points – Style & Documentation Guidelines  

📺 **Video Reference (Hebrew):** [Watch on YouTube](https://www.youtube.com/watch?v=ruZy8gUjX_0)  

This document summarizes the **Java coding style and guidelines**. It covers **comments, identifiers, classes, interfaces, exceptions, separation of concerns, generics, primitive types, IO, strings, memory management, enums, threads, and UI** practices.  

---

## 📝 Comments  

### 1. Javadoc Comments  
- Place **Javadoc comments** for every **class, interface, enum, exception, method, and variable** (excluding local variables).  
- Private classes, methods, and variables may omit Javadoc.  

```java
/**
 *
 *
 */
```  

👉 Proper usage examples can be found in `src.zip` or in the [Oracle Javadoc tutorial](http://www.oracle.com/technetwork/articles/java/index-137868.html).  

### 2. C-style Comments  
- May be placed at the **beginning of a function** to provide detailed information about the algorithm or implementation.  

```java
public void doSomething() {
    /*
     *
     *
     */
}
```

### 3. C++-style Comments  
- Use **inline comments** before logical code blocks.  

```java
// creating gui components
bt = new JButton("ok");
tf = new JTextField(10);

// adding events listeners
bt.addActionListener(...);
```  

---

## 🔑 Identifiers  

1. **Variables & Methods**  
   - Use **lowerCamelCase**.  
   - Abbreviations → all letters capitalized.  
   ```java
   int numOfStudents = 12;
   ```  

2. **Classes/Enums/Exceptions/Interfaces**  
   - Use **UpperCamelCase**.  
   - Abbreviations → all letters capitalized.  
   ```java
   public class SportCar {}
   ```  

3. **Packages**  
   - Use lowercase only.  
   - Start with reversed domain name.  
   ```java
   com.lifemichael.samples
   il.ac.hit.samples
   ```  

---

## 🏗️ Classes  

1. Define a **primary constructor**. Other constructors should call it.  
2. Perform **validation in setters**. Constructors should use setters (no direct assignments).  
3. **Code order**: variables → constructors → methods.  
4. If you override `equals`, you must also override `hashCode`.  
5. Good practice: override `toString`.  
6. When implementing `Cloneable`, override `clone`.  
7. All variables should be **private** unless justified otherwise.  
8. Validate arguments at the **start of each method**.  
9. Always use `@Override` when overriding.  

---

## 🔌 Interfaces  

1. Declare an **interface** and a **separate class** implementing it.  
2. Variables should reference an **interface type**, not a concrete class, where possible.  
   ```java
   List<Currency> currencies = new LinkedList<>();
   ```  
3. Prefer interfaces over abstract classes.  

---

## 🚨 Exception Handling  

1. Avoid catching `Exception` directly.  
2. Define a **project-specific exception** type and use it in method declarations.  

```java
class CurrenciesPlatformException extends Exception {
    CurrenciesPlatformException(String msg, Throwable rootcause) {
        super(msg, rootcause);
    }
}

public interface ICurrenciesModel {
    double convert(double sum, Currency c1, Currency c2)
        throws CurrenciesPlatformException;
}
```  

3. When a code block cannot continue after an exception, wrap the **entire block** in a single `try-catch`.  
4. Fix the code for **runtime exceptions** instead of catching them (except in rare cases like `Double.parseDouble`).  
5. Custom exceptions should provide constructors for:  
   - Message only  
   - Message + root cause (`Throwable`)  

---

## 🔄 Separation of Concerns  

- Keep project layers separated.  
- **No UI logic inside the model**, and vice versa.  

---

## 🧬 Generics  

1. Always use generics properly.  
2. Prefer **bounded wildcards** when possible.  

---

## 🔢 Primitive Types  

- For **financial applications**, avoid using `double` to represent currency.  

---

## 📂 IO Streams  

- Avoid **object serialization**.  
- Prefer saving in **XML**.  

---

## 🔤 Strings  

1. Prefer **StringBuffer/StringBuilder** when mutability is needed.  
2. Prefer explicit string literals (`"abc"`) over `new String("abc")`.  

---

## ♻️ Memory Management  

1. When an object is no longer needed, assign its reference to **null** for GC collection.  
2. Do **not** rely on `finalize()`.  

---

## 🔖 Enum  

- Prefer declaring **enum** over static `int` constants.  

---

## 🧵 Threads  

1. Avoid unnecessary synchronization.  
2. Prefer using **Executors** over creating new `Thread` objects.  

---

## 🖥️ User Interface  

- Every interaction with the UI must occur in the **EDT thread**.  

---

✅ Following these guidelines ensures **clean, maintainable, and professional Java code**.  
