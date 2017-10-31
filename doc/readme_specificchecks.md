[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="specific_checks"></a>
# Specific Checks

  * [Execution overview](#executionOverview)
  * [Create and register your SpecificCheck](#createAndRegister)
  * [JAVA SPI Mapping file](#javaSPI)
  * [How to access to test information during the processing](#testInfo)
 
It could happen that all the expectations that you can add in your JSON input file (see [Check Expectations](readme_expectations.md) for more details), are not enough for your test purposes.
In this case, HEAT comes to your rescue with **"Specific Checks"**.
 
A **"Specific Check"** is a way to perform your checks to requests/responses defined in JSON file through Java code.
Here you'll be able to do whatever you want in programmatic way, but remember that this feature is designed to be used only when [Check Expectations](readme_expectations.md) are not suitable for your purpose, and it is not a reccommanded approach.
 

<a name="executionOverview"></a>
## Execution overview

A "SpecificCheck" can be associated to any HEAT suite, therefore to a whole JSON input file identified by a **< test >** element within the **"testng.xml"** configuration file. You could either associate the same SpecificCheck to more than one HEAT suite.

After this association, the SpecificCheck will be executed **at the end of each test block** within the **"testCases"** JSON attribute.

If you want to **skip the execution** for a given test case within the JSON input file, you could place the logic for this directly inside the SpecificCheck that has the information useful to detect wich is the current test case under analysis.

[![Back to the Top Of Page][upArrow]](#specific_checks)

<a name="createAndRegister"></a>
## Create and register your SpecificCheck

Your own specific check must extends the HEAT core abstract class **com.hotels.heat.core.heatspecificchecks.SpecificChecks** and can be placed wherever you prefer within your project.

```java
public class ExampleSpecificChecks extends SpecificChecks {
    ...
}
```

At this point you have just to implement the **two abstract methods**:

```java
Set<String> handledSuites();
void process(Map testCaseParamenter, Map<String, Response> responsesRetrieved, String testRef, String environment);
```
Through the implementation of **handledSuites()** method, you can associate this SpecificCheck to the proper test suites, simply returning the identifiers of the suites as a Set.

*For example:*

```java
    public Set<String> handledSuites() {
        Set<String> suites = new HashSet<>();
        suites.add("FIRST_SUITE");
        return suites;
    }
```

Through the implementation of **process()** method, you can implement the check that you prefer, using the input parameters.

```java
    public void process(Map testCaseParamenter, Map<String, Response> responsesRetrieved, String testRef, String environment) {
        // here your specific checks!
    }
```

[![Back to the Top Of Page][upArrow]](#specific_checks)

<a name="javaSPI"></a>
### JAVA SPI Mapping file

HEAT , in order to load SpecificCheck classes, uses the Java **Service Provider Interface (SPI)** mechanism.
This means that, for the HEAT abstract class SpecificChecks, we have to create a mapping file in a specially named directory *META-INF/services*.
The name of the file is the name of the SPI class being subclassed, and the file contains the names of the new subclasses of that SPI abstract class.

Therefore, you have to create a put this file here:

![SPI_location](img/javaSPI_file_location.png)


And then, everytime you want add a new own "SpecificCheck", you have to **declare its full class name in the SPI mapping file**.

*For example, inside our SPI mapping file:*

```bash
com.heat.dummypackage.ExampleSpecificChecks
```

[![Back to the Top Of Page][upArrow]](#specific_checks)

<a name="testInfo"></a>
## How to access to test information during the processing

You can implement your specific checks by implementing the **process()** method.

Here you are able to access to all needed data for you purpose, reading them from input parameters, like: 

| Parameter              | Example                                                                                       | Short description                                                                                                                                                                                                           |
|------------------------|-----------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Map testCaseParameter  | ... "queryParameters" -> {"q" -> "London", "APPID" -> "81729a81d49d2ec5d46ae43b602f462c"} ... | The keys are the names of JSON attributes contained in the current "testCases" block. Basing on test modality (Single, Compare, Flow) the values could be primitive values or another attribute-value Map for complex types. |
| Map responsesRetrieved | "Find_Geocode" -> RestassuredResponseImpl,"Find_Distance" -> RestassuredResponseImpl          | SingleMode: The key is the name of "webappUnderTest" (see environments.properties and pom.xml); FlowMode/CompareMode: The keys are the names of "objectName";                                                               |
| String testRef         | "FIRST_SUITE.001"                                                                             | The current suite name (see testng.xml) and test identifier ("testId" attribute)                                                                                                                                            |
| String environment     | "environment1"                                                                                | The name of the environment chosen (-Denvironment command line) during this test execution                                                                                                                                  |

[![Back to the Top Of Page][upArrow]](#specific_checks)

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png













