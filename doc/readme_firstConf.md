[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="generalConfiguration"></a>
# First General Configuration

  * [pom.xml](#single_mode_firstConf_pom)
  * [environment.properties](#single_mode_firstConf_environments)

In this section, we will show you how to configure your HEAT Framework for the first time.

Let's start assuming that each test module is "dedicated" to test a specific service, obviously it can involve other services for integration purposes, but the real target of the module has to be only one service. So please, avoid to create monolithic test modules with more than one target, but take care treating your tests with a 'microservice' approach.


First of all, you have to clone the following "Heat example test module" so that we can customize and use it :

```
>> git clone http://<testModuleRepo> testModule
>> cd testModule/
>> ll
total 16
-rw-r--r--  1 user  AA\BB   599B Mar 31 11:12 environment.properties
-rw-r--r--  1 user  AA\BB   3.6K Mar 31 11:12 pom.xml
drwxr-xr-x  5 user  AA\BB   170B Mar 31 11:12 rules
drwxr-xr-x  4 user  AA\BB   136B Mar 31 11:12 src
```

[![Back to the Top Of Page][upArrow]](#generalConfiguration)

<a name="single_mode_firstConf_pom"></a>
## pom.xml

Let's start modifying the pom.xml.
We have only one dipendency for now, that is the HEAT core and, commented, another possible one, that is an eventual [custom external module](readme_extmodule.md) that can implement some additional features for the specific application users can test.

We have to customise the dependency section and the properties one.

```xml
    <dependencies>
        <dependency>
            <groupId>hcom.funcTest</groupId>
            <artifactId>Heat-Bundle</artifactId>
            <version>1.0.14</version>
        </dependency>
        <!--<dependency>
            <groupId>hcom.funcTest</groupId>
            <artifactId>EXTERNAL MODULE</artifactId>
            <version>1.0</version>
        </dependency>-->
    </dependencies>
```
In the 'dependencies' section please, take care to verify if the version of Heat Bundle (that is a submodule of Heat Core) is always the latest one.

The other part to modify is in the 'properties' section:

```
<!--webapp name-->
<webappUnderTest>GMAPS_DISTANCE</webappUnderTest>

<!-- testng.xml for test parallelization -->
<xmlSuite>testng.xml</xmlSuite>

<defaultEnvironment>environment1</defaultEnvironment>
```
where we have to modify:

*  **webappUnderTest** that is a custom name to identify the service under test. It is not important the nature of this name, it is only an ID, but it is used all over the project.
*  **defaultEnvironment** that is the ID of the environment the test will consider as default (you will have the chance to change it by [command line](readme_commandLine.md), but if you don't this will be the default one)

So, let's assume that we want to identify the service under test with the name 'GMAPS_DISTANCE' and that our default environment is 'environment1'

[![Back to the Top Of Page][upArrow]](#generalConfiguration)

<a name="single_mode_firstConf_environments"></a>
## environment.properties

Let's assume we have three different environments. As a consequence, the service under tests, has three corrispondent paths and this file will contain all those specific urls.

```
GMAPS_DISTANCE.environment1.path=https://maps.googleapis.com/maps/api/distancematrix

GMAPS_DISTANCE.environment2.path=https://maps.googleapis.com/maps/api/distancematrix

GMAPS_DISTANCE.environment3.path=http://httpbin.org
```

The syntax is: `<ID SERVICE>.<ID ENVIRONMENT>.path=<SERVICE URL>`

Please note that the `<SERVICE URL>` can be not only the specific endpoint, but also only the basepath of the service (also in case of services with more than one endpoint).

[![Back to the Top Of Page][upArrow]](#generalConfiguration)


[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png