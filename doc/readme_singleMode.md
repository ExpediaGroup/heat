[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="single_mode"></a>
# Single Mode Request 

  * [testng.xml](#single_mode_firstConf_testngXml)
  * [json input file](#single_mode_firstConf_jsonInputFile)
  * [first run](#single_mode_firstConf_firstRun)
  * [multipart request](#single_mode_firstConf_multipart_request)

<a name="single-mode_example"></a>
Let's have a first practical example. You have to make a test on a service like distancematrix json endpoint of Google Maps (url: [https://maps.googleapis.com/maps/api/distancematrix/json](https://maps.googleapis.com/maps/api/distancematrix/json) ) passing four query parameters that are units, origins, destinations and key (needed only to use the service).

The curl is:

`curl -X GET "https://maps.googleapis.com/maps/api/distancematrix/json?units=meters&origins=taranto%2C%20italia&destinations=roma%2C%20italia&key=AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk"`

The retrieved response is:

```json
{
    "destination_addresses": [
        "Roma, Italia"
    ],
    "origin_addresses": [
        "Taranto TA, Italia"
    ],
    "rows": [
        {
            "elements": [
                {
                    "distance": {
                        "text": "512 km",
                        "value": 511726
                    },
                    "duration": {
                        "text": "5 ore 24 min",
                        "value": 19449
                    },
                    "status": "OK"
                }
            ]
        }
    ],
    "status": "OK"
}
```

Let's suppose that we want to check that the "status" field retrieved in the response is "OK".

[![Back to the Top Of Page][upArrow]](#single_mode)


<a name="single_mode_firstConf_testngXml"></a>
## testng.xml

The next step is modifying the testng.xml file, the real driver of our tests. The file is in `src/main/resources` folder and it contains some general configurations and the definition of all the suites we want to run.

First of all we have to give a name to the suite collection and to define the number of maximum number of suites we want to run in parallel:

```
<suite name="HEAT RUN" parallel="tests" thread-count="1">
```

In this example, the name is 'HEAT RUN' and we run only one test suite a time.

The 'listeners' block is **untouchable** and it contains some core classes useful for logging (_CustomTestNgListener_) and for the final test report (_CustomJUnitReportListener_):

```
    <!-- Please do not touch this section: START -->
    <listeners>
        <listener class-name="com.hotels.restassuredframework.core.listeners.CustomTestNgListener"/>
        <listener class-name="com.hotels.restassuredframework.core.listeners.CustomJUnitReportListener" />
    </listeners>
```
There is also a section in which we define the path of the `environment.properties` file (if we like, we could change the name of the property file, but please take care to use consistent paths all over the project configuration):

```
<parameter name="envPropFilePath" value="environment.properties"/>
```
After this general part, we can start writing the configuration for our first test suite:

```
    <test name="GOOGLE_MAPS_ENV1" enabled="true">
        <parameter name="inputJsonPath" value="/testCases/GmapsSingleModeTestCases.json"/>
        <parameter name="enabledEnvironments" value="environment1"/>
        <classes>
            <class name="com.hotels.restassuredframework.core.runner.SingleMode"/>
        </classes>
    </test>
```
We have to define the following fields:

*  test name (`test name`) that will be the ID of the suite we are going to write.
*  is test suite enabled? (`enabled="true"`) as we could write down some suites that we don't want to execute for some reasons
*  the environment IDs for which the test suite is enabled to run (for example, when we test a booking form, it is reasonable not to enable the test in production environment but only in test ones). It is a list of IDs separated by commas.
*  the path of the json input file with all the test cases of the suite (`inputJsonPath`). The root folder for these json files is `src/test/resources` so, in this example, the json input file complete path is `src/test/resources/testCases/GmapsSingleModeTestCases.json`
*  The execution modality [SingleMode](readme_singleMode.md), [CompareMode](readme_compare.md) or [FlowMode](readme_flow.md) that in this case is `com.hotels.restassuredframework.core.runner.SingleMode` (it is the path of a class in the Heat Core Module)

Now that we have created the driver for our test suite, let's write down the json input file with all the specific test cases!

[![Back to the Top Of Page][upArrow]](#single_mode)

<a name="single_mode_firstConf_jsonInputFile"></a>
## JSON Input File (test cases)

This is the place in which you can write your test cases.

It contains some json blocks:

* the first ones are only for general settings and common variables;
* the last ones are the real test cases with all the input parameters useful for the request against the service under test and with the expectations for each test case.

```
{
    "testSuite": {
        "generalSettings": {
            "httpMethod": "GET",
            "suiteDesc":"Example Single Mode Tests"
        },
        "preloadVariables": {
            "API_KEY":"AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk"
        },
```
**generalSettings** block contains only the httpMethod used for the requests to the service (it is common for all TCs in the suite. If you need to have different methods for more endpoints, it will be necessary to create another json file) and a general test suite description (useful for logging)

**preloadVariables** block (_optional_) contains some variables common used in the TCs in the suite, so that we can avoid not nice typo and unuseful re-typings.

```        
        "jsonSchemas": {
            "correctResponse": "schemas/okCase.json",
            "errorResponse": "schemas/notOkCase.json"
        },
```

<a name="single-mode_firstConf_jsonInputFile_jsonSchemas"></a>
**jsonSchemas** block (_optional_) contains some mapping between IDs of json schemas and their paths (the root path is `src/test/resources` so the complete path of 'correctResponse' will be `src/test/resources/schemas/okCase.json`). These IDs can be used among the expectations, as we have the possibility to check if the response retrieved from the service under test is compliant with a given json schema.

```
        "testCases": [
            .....
        ]
```
Now there is the **testCases** json array, and each element inside of it is a test case.

Each test case is defined as follows:

```
          {
                "testId": "001",
                "testName": "single mode test for new heat #1",
                "url": "/json",
                "queryParameters": { .... },
                "headers": { .... },
                "cookies": { .... },
                "expects": {
                    "responseCode": "200",
                    "jsonSchemaToCheck": "correctResponse",
                    "fieldCheck": [ .... ],
                    "headerCheck": { .... },
                    "cookieCheck": { .... }
                }
            }
```
**testId** is the ID of the test case inside the suite. In this way, each test case has a unique identification among all the test cases in the project, as we define it as `<SUITE NAME>.<TEST CASE ID>`. It is necessary for logging so that we can attribute a log raw to the specific running test case.

**testName** is a description of the test case. It is not important to make it short, but it is very important to make it meaningful

**url** is the specialization of the base path specified in the [environment.properties](readme_firstConf.md) file. 

* For example if the service we want to test has a basepath defined as `http://foo.xyz` (according to the specific environment we are using) and we want to test a particular endpoint called `endpointFoo.json`, the **url** parameter will contain the string `/endpointFoo.json`. In this way, the complete path will be the concatenation of the two strings: `http://foo.xyz/endpointFoo.json`.
* If the [environment.properties](readme_firstConf.md) already contains the complete url, we can leave the **url** parameter empty.

**queryParameters** block contains all the query parameters to send.

* In case of GET requests, it will be like a map string-string like the following one:

```
"queryParameters": {
    "param1": "value1",
    "param2":"value2"
    }
```
so that the final request will be something like `curl -X GET 'http://foo.xyz/endpointFoo.json?param1=value1&param2=value2'`

* In case of POST requests, it has to contain a unique value called `postBody' with the body of the request

```
"queryParameters": {
    "postBody": "This is a post body"
    }
```
so that the final request will be something like `curl -X POST http://foo.xyz/endpointFoo.json -d 'This is a post body'`

For the post body, you can also refer to a specific external json file (in case of huge bodies) simply specifying the file path

```
"queryParameters": {
    "postBody": "/postbody/post001.json"
    }
```
where the root path is `src/test/resources` so that the complete path of the json file we are referring to is `src/test/resources/postbody/post001.json`. Up to now we support only `json` file extension.

* In case of POST requests with multipart body (for file upload support), it has to contain a unique value called `parts` with an array of the varios values

```
"parts": [
    {
      "name": "simpleField",
      "value": "simpleValue"
    },
    {
      "name": "fileToUpload",
      "file": "/files/attachment.txt",
      "contentType": "text/plain"
    }
]
```
When defining a part using the `value` property it should contains the plain value of the parameter, meanwhile when using the `file` property it should contains a reference to a file (based on the application classpath).
The `contentType` can be omitted, in that case the framework will try to select the more appropriate automatically.

**headers** block (_optional_) contains a map string-string of the headers to send in the request

```
"headers": {
        "headerName": "headerValue"
    }
```

**cookies** block (_optional_) contains a map string-string of the cookies to send in the request

```
"cookies": {
        "cookieName": "cookieValue"
    }
```

**expects** block contains all the expectations in terms of:

* **responseCode (optional)** that is the http response code expected from the request just made. [Official w3e.org documentation on response code values](https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html)

```
"responseCode": "200"
```
*NOTE: if you don't specify the "responseCode" field, it's valued with "200" as default.*

* **jsonSchemaToCheck** (_optional_) is the ID of the json schema to use to verify the correctness of the obtained response. Please refer to [jsonSchemas block](#single-mode.firstConf.jsonInputFile.jsonSchemas) to define the IDs.

```
"jsonSchemaToCheck": "correctResponse"
```

* **fieldCheck** is an array of puntual expectations on the response. Each element will be a check on the response and, if one fails, the test case will stop and will be signed as 'failed' in the log, going on to the next test case in the suite. Please refer to [expectation field check](readme_expectations.md) for all the possible checks to make.

```
"fieldCheck": [
        {
            "description": "status has to be REQUEST_DENIED",
            "actualValue": "status",
            "expectedValue": "REQUEST_DENIED"
        }
	]
```

* **headerCheck** (_optional_) contains a map string-string of the headers to check in the response. The key of the map is the name of the header to check and the value of the map is the expected value of the header.

```
"headerCheck": {
        "Content-Type": "application/json; charset=UTF-8"
    }
```

* **cookieCheck** (_optional_) contains a map string-string of the cookies to check in the response. The key of the map is the name of the cookie to check and the value of the map is the expected value of the cookie.

```
"cookieCheck": {
        "cookieName": "expectedCookieValue"
    }
```
[![Back to the Top Of Page][upArrow]](#single_mode)

<a name="single_mode_firstConf_firstRun"></a>
### First Run

So, going back to the [example](#single-mode.example), and let's put all the info together.

#### testng.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="HEAT RUN" parallel="tests" thread-count="1">

    <!-- Please do not touch this section: START -->
    <listeners>
        <listener class-name="com.hotels.restassuredframework.core.listeners.CustomTestNgListener"/>
        <listener class-name="com.hotels.restassuredframework.core.listeners.CustomJUnitReportListener" />
    </listeners>
    <parameter name="envPropFilePath" value="environment.properties"/>
    <!-- Please do not touch this section: END -->
    
    <test name="GOOGLE_MAPS_ENV1" enabled="true">
        <parameter name="inputJsonPath" value="/testCases/GmapsSingleModeTestCases.json"/>
        <parameter name="enabledEnvironments" value="environment1"/>
        <classes>
            <class name="com.hotels.restassuredframework.core.runner.SingleMode"/>
        </classes>
    </test>
    
</suite>    
```

#### json input file

The json input file (/testCases/GmapsSingleModeTestCases.json) will be something like:

```
{
    "testSuite": {
        "generalSettings": {
            "httpMethod": "GET",
            "suiteDesc":"Example Single Mode Tests"
        },
        "jsonSchemas": {
            "correctResponse": "schemas/okCase.json"
        },
        "testCases": [
            {
                "testId": "001",
                "testName": "single mode test for new heat #1",
                "url": "/json",
                "queryParameters": {
                    "units": "meters",
                    "origins":"via Atenisio Carducci 10, Taranto Italia",
                    "destinations":"via dei Giuochi Istmici 40, Roma Italia",
                    "key":"AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk"
                },
                "headers": {
                    "Cache-Control": "no-cache"
                },
                "expects": {
                    "responseCode": "200",
                    "jsonSchemaToCheck": "correctResponse",
                    "fieldCheck": [
                        {
                            "description": "result has to be OK",
                            "actualValue": "${path[status]}",
                            "expectedValue": "OK"
                        }
						],
                    "headerCheck": {
                        "Content-Type": "application/json; charset=UTF-8"
                    	}
                		}
            		}
	        ]
        }
    }                                        
```
#### json schema to check
The json schema (schemas/okCase.json) to check will contain the requested json schema (please have a look at the Heat Test Module Example to see the complete file)

#### environment settings
The environment.properties file will contain only a raw:

`GMAPS_DISTANCE.environment1.path=https://maps.googleapis.com/maps/api/distancematrix`

#### pom.xml
The pom file will contain the following settings:

`<webappUnderTest>GMAPS_DISTANCE</webappUnderTest>`
`<defaultEnvironment>environment1</defaultEnvironment>`

#### test run
Now we have to run tests.
First of all we have to be sure that all packages are correctly downloaded by maven:

`mvn clean install -DskipTests`

then we can properly run tests:

`mvn test`

Please, refer to the [command line](readme_commandLine.md) section for other details on accepted options.

[![Back to the Top Of Page][upArrow]](#single_mode)

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png





