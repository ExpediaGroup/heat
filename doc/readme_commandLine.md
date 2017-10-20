[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="commandLine"></a>
# Test run and debug with command line

  * [Select the environment](#environment)
  * [Set the log level](#logLevel)
  * [Run just the tests you need](#specific)


To run tests, first, we have to be sure that all the packages are correctly downloaded from the pom

```
mvn clean install -DskipTests
```

<a name="environment"></a>
## Select the environment
When you want to run tests, you usually have to decide the environment you want to test. In the [environment.properties](readme_firstConf.md) file we have defined some environments, with the corresponding path.

If you don't specify the environment to test, the default one will be used (defined in [pom.xml](readme_singleMode.md)). Otherwise, we can change it by command line:

```
mvn test -Denvironment=anotherEnvironment
```
[![Back to the Top Of Page][upArrow]](#commandLine)

<a name="logLevel"></a>
## Set the log level
The default log level for the output console log is 'INFO', but if you need a different level, you can change it with the command line:

```
mvn test -DlogLevel=DEBUG
```

The allowed values are: `ERROR`, `WARN`, `INFO` (_default_) , `DEBUG`, `TRACE`

The INFO level will show you only 'SUCCESS' if the test case is successful, otherwise 'FAILED' with the specific error message occurred.

[![Back to the Top Of Page][upArrow]](#commandLine)

<a name="specific"></a>
## Run just the tests you need
You can choose to run specific test suites and/or test cases.

To do that you can use the `heatTest` system property, that contains the list of what you want to run, using the comma as separator.

For example, to run only specific test suites: 
`mvn test -DheatTest="SUITE1,SUITE2"`

To run a list of specific test cases (also belonging to different suites): `mvn test -DheatTest="SUITE1.001,SUITE1.002,SUITE2.001"`

Obviously you can match these examples and specify if you want to run some test suites and some test cases: `DheatTest="SUITE1,SUITE2.001,SUITE2.002"`

[![Back to the Top Of Page][upArrow]](#commandLine)

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png
