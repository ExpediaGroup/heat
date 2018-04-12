[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="placeholders"></a>
# Placeholders

  * [Path](#path)
  * [Preload](#preload)
  * [GetStep](#getstep)
  * [Cookie](#cookie)
  * [Header](#header)
  * [SysProp](#sysprop)
  * [Present / NotPresent](#present)
  * [Today](#today)
  * [Wiremock](#wiremock)

In order to make data dynamic and easier to use in the JSON input files, we introduced some placeholders

|             Placeholder             |                                            Short Description                                            |
|:-----------------------------------:|:-------------------------------------------------------------------------------------------------------:|
| `${path[...]}`                       | retrieves the value of a specific field from a json response                                            |
| `${preload[...]}`                     | retrieves the value of one of the elements present in the 'preloadVariables' section                    |
| `${cookie[...]}`                      | retrieves the value of a cookie from a json response                                                    |
| `${header[...]}`                      | retrieves the value of a header from a json response                                                    |
| `${getStep(...).getOutputParam(...)}` | in flow mode tests, it retrieves a specific output parameter coming from a specific step of the same tc |
| `${SysProp[...]}`                     | retrieves the value of a particular system property passed by 'mvn run' command line                    |
| `${Present}`                          | used in [assertions](readme_expectations.md) to check if a specific field of a json response is present and has not null value           |
| `${NotPresent}`                       | used in [assertions](readme_expectations.md) to check if a specific field of a json response is not present or has null value      |
| `${TODAY....}`                        | retrieves a string representing a data, customisable in terms of format                                 |


<a name="path"></a>
## Path

'Path' placeholder is used to execute a specific query over a JSON string using a JSONPath (GPATH) query.<br/>
The syntax is:

    `${path[<PATH>]}`

or

    `${path[<JSON_STRING>, <PATH>]}`

**Description:**<br/>
`<PATH>` is the GPath string describing the query <br/>
`<JSON_STRING>` is the JSON which the `<PATH>` is applied

NOTE 1: A common usage to pass `<JSON_STRING>` is using a `$preload` variable: <br/>
```json
"actualValue": "${path[${preload(MY_JSON)},my.path.to.field]}"
```
NOTE 2: **if `<JSON_STRING>` is not specified, the service response is used**<br/>

**Additional information:**<br/>
To understand how we can use this important placeholder, let's have a look at how RestAssured treats a response and how we can navigate it.
A JSON response is treated as a `com.jayway.restassured.response.Response` object (official documentation [here](https://static.javadoc.io/com.jayway.restassured/rest-assured/2.4.0/com/jayway/restassured/response/Response.html)) so, in order to navigate the body of a response we use the following code:

```java
Response rsp = ...
String pathToNavigate = "..."
JsonPathConfig config = new JsonPathConfig(JsonPathConfig.NumberReturnType.BIG_DECIMAL);
String field = rsp.jsonPath(config).get(pathToNavigate).toString();
```

where `com.jayway.restassured.path.json.config.JsonPathConfig` is a configuration for the JSON path retrieving (official documentation [here](http://static.javadoc.io/com.jayway.restassured/json-path/2.8.0/com/jayway/restassured/path/json/config/JsonPathConfig.html)), `rsp` is the response to analyse (`Response` object) that we transform in a `com.jayway.restassured.path.json.JsonPath` object (official documentation [here](https://github.com/json-path/JsonPath)) and `pathToNavigate` is the 'address' we use to reach a specific object in the given response.

For example, if we have a response whose JSON body is

```json
{
	"status":"OK",
	"number":1,
	"boolean":true,
	"simpleArray":[
		"firstString":"one",
		"secondString":"two"
	],
	"objectArray":[
		{
			"firstElement":1,
			"secondElement":"two"
		},
		{
			"firstElement":2,
			"secondElement":"hello"
		}
	]
}
```

If we wanted to retrieve the 'status' element, we will set the 'pathToNavigate' to `"status"` because this element is in first level of the JSON, and the output 'field' would be `"OK"`.

In the same way we can set 'pathToNavigate' in the following ways:

| field to retrieve                                   | pathToNavigate value          | path placeholder (usable json input file)                    | output                                                                                                  |
|-----------------------------------------------------|-------------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------|
| status                                              | "status"                      | ${path[status]}                      | "OK"                                                                                                    |
| number                                              | "number"                      | ${path[number]}                      | "1"                                                                                                     |
| boolean                                             | "boolean"                     | ${path[boolean]}                     | "true"                                                                                                  |
| simpleArray                                         | "simpleArray"                 | ${path[simpleArray]}                 | "[\"firstString\":\"one\",\"secondString\":\"two\"]"                                                    |
| firstString                                         | "simpleArray.firstString"     | ${path[simpleArray.firstString]}     | "one"                                                                                                   |
| secondString                                        | "simpleArray.secondString"    | ${path[simpleArray.secondString]}    | "two"                                                                                                   |
| objectArray                                         | "objectArray"                 | ${path[objectArray]}                 | "[{\"firstElement\":1,\"secondElement\":\"two\"},{\"firstElement\":\"2\",\"secondElement\":\"hello\"}]" |
| all 'firstElement' in the 'objectArray'             | "objectArray.firstElement"    | ${path[objectArray.firstElement]}    | "[1,2]"                                                                                                 |
| all 'secondElement' in the 'objectArray'            | "objectArray.secondElement"   | ${path[objectArray.secondElement]}   | "[\"two\",\"hello\"]"                                                                                   |
| 'firstElement' in the first object of 'objectArray' | "objectArray[0].firstElement" | ${path[objectArray[0].firstElement]} | "1"                                                                                                     |

Please, note that whatever the nature of the field value is, **the output we retrieve will be always a _String_**.


We can also make some simple operation, allowed by JsonPath (official documentation [here](https://github.com/json-path/JsonPath#filter-operators)), for example we can retrieve the size of an array:

`${path[simpleArray.size()]}`

and the output will the the string "2", because 'simpleArray' has two elements.

JsonPath is a powerful instrument, because it supports **groovy implementation** and, thanks to that, we can run more complex stuff such as retrieve the element `objectArray[x].firstElement` where `objectArray[x].secondElement` is equal to "hello":

`${path[objectArray.findAll{item -> item.secondElement == 'hello'}[0].firstElement]}`

and the output will be the string "2".

Another feature of this placeholder is that, if you want to retrieve the entire response as a string, you can simply specify `${path[.]}`

[![Back to the Top Of Page][upArrow]](#placeholders)

<a name="preload"></a>
## Preload
Preload placeholder is used to refer to a variable declared in the 'preloadVariables' section of the JSON input file.

For example, the json file can be written as follows:

```json
{
    "testSuite": {
        "generalSettings": {
            "httpMethod": "GET",
            "suiteDesc":"Example Single Mode Tests"
        },
        "preloadVariables": {
            "CHECK_IN":"${TODAY+100_YYYY-MM-dd}",
            "CHECK_OUT":"${TODAY+101_YYYY-MM-dd}",
            "API_KEY":"AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk"
        },
        .....
        "testCases": [
            {
                "testId": "001",
                "testName": "single mode test for new heat #1",
                "url": "/json",
                "queryParameters": {
                    "units": "meters",
                    "origins":"via Galileo Galilei 15, Taranto Italia",
                    "destinations":"via dei Giuochi Istmici 40, Roma Italia",
                    "key":"${preload[API_KEY]}"
                },  
                ....      
```
In this way we have declared the variable in the 'preloadVariables' section as `"API_KEY":"AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk"` and we use it simply referring to its name `"key":"${preload[API_KEY]}"`.

There are two advantages coming from this approach. The first one is that we can avoid some typo in writing always the same string. The second one is a great performance advantage: if the preloadVariable contains some elaborations coming from a custom external module (see doc [here](externalModules.md)), we can do it only once and not each time we write the entire placeholder.

[![Back to the Top Of Page][upArrow]](#placeholders)

<a name="getstep"></a>
## GetStep
This placeholder is very useful in **_flow mode_**, since it allows us to use a parameter coming from a step in another following step of the same test case.

For example:

```json
{
  "testSuite": {
    "generalSettings": {
      "suiteDesc": "Example Flow Mode Tests",
      "flowMode": "true"
    },
        "preloadVariables": {
        "outputRspFormat":"json",
        "DISTANCE_API_KEY":"AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk",
        "GEOCODE_API_KEY":"AIzaSyBHOMI_1PF4ag943jCgIavFtGYN5lJn61I" 
    },
    "testCases": [
      {
        "testId": "001",
        "testName": "Generic test on flow mode",
        "e2eFlowSteps": [
          {
            "objectName": "Find_Distance",
            "stepNumber": "1",
            "webappName": "GMAPS_DISTANCE",
            "httpMethod": "GET",
            "testName": "first step",
            "url": "/${preload[outputRspFormat]}",
                "queryParameters": {
                    "units": "meters",
                    "origins":"via Galileo Galilei 15, Taranto Italia",
                    "destinations":"via dei Giuochi Istmici 40, Roma Italia",
                    "key":"${preload[DISTANCE_API_KEY]}"
                },
            "headers":
            {
              "Cache-Control": "no-cache"
            },   
            "expects": {
            	 "responseCode": "200", 
            	 "fieldCheck": [],
             }
        	 "outputParams" : {
        	 	"origin_addresses" : "${path[origin_addresses[0]]}"
            }
        },
      {
        "objectName": "Find_Geocode", 
        "stepNumber": "2",
        "webappName": "GMAPS_GEOCODE",
        "httpMethod": "GET",
        "url": "/json",
        "testName": "second step",
        "queryParameters": {
          "address": "${getStep(1).getOutputParam(origin_addresses)}", 
          "key": "${preload[GEOCODE_API_KEY]}"
        }, 
        ...      
```
In this way we did a first request from which we retrieve a parameter (in this case it is a field of the response)

```
"outputParams" : {
 	"origin_addresses" : "${path[origin_addresses[0]]}"
}	
```
and we use it in the following step as query parameter:
```
"queryParameters": {
  "address": "${getStep(1).getOutputParam(origin_addresses)}"
}
```

The syntax is `${getStep(<Step Number>).getOutputParam(<Param Name>)}` where `<Step Number>` is the number of the step in which we have declared the parameter we want to use, and `<Param Name>` is the name of the parameter we want to retrieve.

[![Back to the Top Of Page][upArrow]](#placeholders)

<a name="cookie"></a>
## Cookie
This placeholder is very simple and it allows to retrieve a specific cookie coming from the response just obtained from the service under test.

It can be useful, for example, in the flow mode, comparing two cookies coming from two different steps.

```json
{
  "testSuite": {
    "generalSettings": {
      "suiteDesc": "Example Flow Mode Tests",
      "flowMode": "true"
    },
        "preloadVariables": {
        "outputRspFormat":"json",
        "DISTANCE_API_KEY":"AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk",
        "GEOCODE_API_KEY":"AIzaSyBHOMI_1PF4ag943jCgIavFtGYN5lJn61I" 
    },
    "testCases": [
      {
        "testId": "001",
        "testName": "Generic test on flow mode",
        "e2eFlowSteps": [
          {
            "objectName": "Find_Distance",
            "stepNumber": "1",
            "webappName": "GMAPS_DISTANCE",
            "httpMethod": "GET",
            "testName": "first step",
            "url": "/${preload[outputRspFormat]}",
                "queryParameters": {
                    "units": "meters",
                    "origins":"via Galileo Galilei 15, Taranto Italia",
                    "destinations":"via dei Giuochi Istmici 40, Roma Italia",
                    "key":"${preload[DISTANCE_API_KEY]}"
                },
            "headers":
            {
              "Cache-Control": "no-cache"
            },   
            "expects": {
            	 "responseCode": "200", 
            	 "fieldCheck": [],
             }
        	 "outputParams" : {
        	 	"origin_addresses" : "${path[origin_addresses[0]]}",
            	"cookieValue" : "${cookie[cookieName]}"
            }
        },
      {
        "objectName": "Find_Geocode", 
        "stepNumber": "2",
        "webappName": "GMAPS_GEOCODE",
        "httpMethod": "GET",
        "url": "/json",
        "testName": "second step",
        "queryParameters": {
          "address": "${getStep(1).getOutputParam(origin_addresses)}", 
          "key": "${preload[GEOCODE_API_KEY]}"
        },
        "cookies":
        {
          "cookieName": "${getStep(1).getOutputParam(cookieValue)}"
        },  
        ...      
```

<a name="header"></a>
## Header
This placeholder totally the same as the `Cookie` one but it refers to headers.

[![Back to the Top Of Page][upArrow]](#placeholders)

<a name="sysprop"></a>
## SysProp
The SysProp placeholder is useful in case the user needs to pass a variable via command line as system property.

The syntax is `${SysProp[<Property Name>,<default value>]}` where the `<Property Name>` is the name of the property we want Heat to use, and `<default value>` is the value assigned to the variable if we don't pass any specific input.

For example, to run a suite, we can execute the following command:

```
mvn test -Denvironment=env1 -DheatTest=SUITE.001 -DmyProperty=myValue
```
and in the JSON input file we can find

```json
{
  "testSuite": {
    "generalSettings": {
      "suiteDesc": "Example Flow Mode Tests",
      "flowMode": "true"
    },
        "preloadVariables": {
        "outputRspFormat":"json",
        "DISTANCE_API_KEY":"AIzaSyDuJvGUBixcL3uzS4dDVtDE-jex24F0BFk",
        "GEOCODE_API_KEY":"AIzaSyBHOMI_1PF4ag943jCgIavFtGYN5lJn61I",
        "EXTERNAL_PROPERTY":"${SysProp[myProperty,default_value]}"
    },
    ...
```    
In this way, the variable `EXTERNAL_PROPERTY` will be set to `myValue`.
If we did not pass any `-DmyProperty` property in command line, the variable `EXTERNAL_PROPERTY` would be set to `default_value`.

[![Back to the Top Of Page][upArrow]](#placeholders)

<a name="present"></a>
## Present / NotPresent
This placeholder is deeply explained in the [expectation section](readme_expectations.md) and is used to verify if a specific field is present or not present in the JSON body of the response retrieved from the service under test.

[![Back to the Top Of Page][upArrow]](#placeholders)

<a name="today"></a>
## Today
Today placeholder is useful to write dates without hardcoding a specific one, with the possibility to indicate a [format](https://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html).

The syntax is `${TODAY_<Format>}` or `${TODAY+<NumberOfDaysToAdd>_<Format>}`.

For example we want to write today date with the format YYYY-MM-dd (i.e. '2017-07-14'). The placeholder will be written as: 

```
${TODAY_YYYY-MM-dd}
```
In this way the value will be always updated to the current date.

Another possibility is to add a specific number of days to the current one, for example in 100 days (i.e. '2017:07:24'):

```
${TODAY+100_YYYY:MM:dd}
```

[![Back to the Top Of Page][upArrow]](#placeholders)

<a name="wiremock"></a>
## Wiremock


[![Back to the Top Of Page][upArrow]](#placeholders)

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png


