# Order Processing Java Application

A basic Java application setup with Maven.

## Project Structure

```
src/
  main/
    java/
      Main.java    # Main application class
```

## Building and Running

This is a Maven project. To build and run:

1. Build the project:
   ```bash
   mvn clean install
   ```

2. Run the application:
   ```bash
   mvn exec:java -Dexec.mainClass="Main"
   ```

## Requirements
- Java 17 or higher
- Maven