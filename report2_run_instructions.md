## Run Instructions

After unpacking the ZIP archive, import the project as a Maven project in Eclipse,
IntelliJ IDEA, or Visual Studio Code. The project uses Java 17 and Maven.

### Run the application

From the project root, run:

```bash
mvn javafx:run
```

This starts the JavaFX user interface. The system includes the developer `huba`, as required
by the project description. As an employee, the user can create a project, create activities
for the project, assign developers to activities, register time on activities, register absence,
view available developers, assign a project leader, set deadlines, view project progress, and
generate project report information.

### Run all tests

From the project root, run:

```bash
mvn test
```

This executes both the Cucumber BDD scenarios and the JUnit white-box tests.

### Generate code coverage

From the project root, run:

```bash
mvn test jacoco:report
```

After the command finishes, the JaCoCo coverage report can be found in:

```text
target/site/jacoco/index.html
```
