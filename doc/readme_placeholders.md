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
| `${preload[...]}`                     | retrieves the value of one of the elements present in the 'beforeTestSuite' and 'beforeStep' sections                    |
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
Preload placeholder is used to refer to a variable declared in the 'beforeTestSuite' and 'beforeStep' sections of the JSON input file.

For example, the json file can be written as follows:

```json
{
  "testSuite": {
    "generalSettings": {
      "suiteDesc": "Example Flow Mode Tests",
      "flowMode": "true"
    },
    "beforeTestSuite": {
      "WM_REQUESTS" : "beforeTestSuite_value",
      "MYVAR_1" : "valueSuite1",
      "MYVAR_2" : "valueSuite2"
    },
    "testCases": [
      {
        "testId": "001",
        "testName": "Test beforeTestSuite and beforeStep scopes",
        "e2eFlowSteps": [
          {
            "stepNumber": "1",
            "objectName": "beforeStep variable with the same name of beforeTestSuite variable",
            "beforeStep" : {
              "MYVAR_1" : "valueStep1"
            },
            "webappName": "FAKEAPI",
            "httpMethod": "GET",
            "url": "/users",
            "queryParameters": {},
            "headers": {},
            "expects": {
              "responseCode": "200",
              "fieldCheck": [
                {
                  "description": "Overwritten var",
                  "actualValue": "${preload[MYVAR_1]}",
                  "expectedValue": "valueStep1"
                },
                {
                  "description": "Not Overwritten var",
                  "actualValue": "${preload[MYVAR_2]}",
                  "expectedValue": "valueSuite2"
                }
              ]
            }
          }

```
In this way we declared the variable in 'beforeTestSuite' section as `"MYVAR_2":"valueSuite2"` and we use it simply referring to its name `"key":"${preload[MYVAR_2]}"`

At the same way we declared the variable in 'beforeStep' section as `"MYVAR_1":"valueStep1"` and we use it simply referring to its name `"key":"${preload[MYVAR_1]}"`

Please notice that the value of MYVAR_1 in beforeStep overrides the one declared in beforeTestSuite

There are two advantages coming from this approach. The first one is that we can avoid some typo in writing always the same string. The second one is a great performance advantage: if the beforeTestSuite contains some elaborations coming from a custom external module (see doc [here](externalModules.md)), we can do it only once and not each time we write the entire placeholder.

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
        "beforeTestSuite": {
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
        "beforeTestSuite": {
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
        "beforeTestSuite": {
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
HEAT provides a set of utilities to easily use Wiremock in test cases (more info on Wiremock here: http://wiremock.org/).

- ${wiremock[WM_INSTANCE].resetRequests}
- ${wiremock[WM_INSTANCE].resetScenarios}
- ${wiremock[WM_INSTANCE].requests}


Here the exhaustive list:

  **Reset utilities**

 * The following example shows how to reset Wiremock cache (equivalent to perform a POST call to __admin/requests/reset endpoint) 
```json
{
        "testId": "002",
        "testName": "Test $wiremock.resetRequests feature - get(total) - get(response) - get(status) - default getter",
        "e2eFlowSteps": [
          {
            "stepNumber": "1",
            "objectName": "Reset wiremock, then ask for total, response and default",
            "beforeStep" : {
              "WM_RESET" : "${wiremock[WM_INSTANCE].resetRequests}"
            },
            "webappName": "FAKEAPI",
            "httpMethod": "GET",
            "url": "/users",
            "queryParameters": {},
            "headers": {},
            "expects": {
              "responseCode": "200",
              "fieldCheck": [
                {
                  "description": "Check that WM_RESET response has an empty string",
                  "actualValue": "${preload[WM_RESET].get(response)}",
                  "expectedValue": ""
                },
                {
                  "description": "Check that WM_RESET default oject is the same of get(response)",
                  "actualValue": "${preload[WM_RESET]}",
                  "expectedValue": ""
                },
                {
                  "description": "Check that WM_RESET status has HTTP status code returned of wiremock server",
                  "actualValue": "${preload[WM_RESET].get(status)}",
                  "expectedValue": "200"
                }
              ]
            }
```

The value of **WM_RESET** is returned in beforeStep and we can perform the following operation on it:
 ```
 ${preload[WM_RESET].get(response)}
 ```
 In order to get the response. Please note that the above is equivalent to **${preload[WM_RESET]}** because *get(response)* method is the default one.

The **get(status)** can be used to retrieve the http response status:
 ```
 ${preload[WM_RESET].get(status)}
 ```

 **Requests utilities**
 
 The following example shows hot to call Wiremock cache to retrieve response and http status:
 
```json
"beforeStep" : {
              "WM_REQUESTS" : "${wiremock[WM_INSTANCE].requests}"
            },
            "webappName": "FAKEAPI",
            "httpMethod": "GET",
            "url": "/users",
            "queryParameters": {},
            "headers": {},
            "expects": {
              "responseCode": "200",
              "fieldCheck": [
                {
                  "description": "Check that WM_REQUESTS meta.total has the correct result",
                  "actualValue": "${preload[WM_REQUESTS].get(total)}",
                  "expectedValue": "1"
                },
                {
                  "description": "Check that WM_REQUESTS response has a JSON with not matched result",
                  "actualValue": "${preload[WM_REQUESTS].get(response)}",
                  "expectedValue": ["\"wasMatched\" : false"]
                },
                {
                  "description": "Check that WM_REQUESTS default oject is the same of get(response)",
                  "actualValue": "${preload[WM_REQUESTS]}",
                  "expectedValue": ["\"wasMatched\" : false"]
                },
                {
                  "description": "Check that WM_REQUESTS status has HTTP status code returned of wiremock server",
                  "actualValue": "${preload[WM_REQUESTS].get(status)}",
                  "expectedValue": "200"
                }
              ]
```
The value of **WM_REQUESTS** is returned in beforeStep and we can perform the following operation on it:
 ```
 ${preload[WM_REQUESTS].get(total)}
 ```
to get the number of request intercepted by Wiremock. This corresponds to the **meta.total** value returned when calling Wiremock __admin/requests endpoint
 ```
  ${preload[WM_REQUESTS].get(response)}
  ```
which returns the response body. This is the same as **${preload[WM_REQUESTS]}** because *get(response)* can be omitted.

The **get(status)** can be used to retrieve the http response status:
 ```
 ${preload[WM_REQUESTS].get(status)}
 ```
 
 **Reset scenario**
 
In order to reset Wiremock scenario the following command can be used:
```
"${wiremock[WIREMOCK_LOCALIZATION].resetScenarios}"
```
Example of usage:
```json
"e2eFlowSteps": [
                    {
                        "objectName": "Set Wm Localization scenario to 'randomdata'",
                        "stepNumber": "1",
                        "beforeStep" : {
                         "WM_RESET_SERVICE1" : "${wiremock[SERVICE_1].resetRequests}",
                         "WM_RESET_SCENARIO2" : "${wiremock[SERVICE_2].resetScenarios}"
                        },
                    },
```
In this way we can save the value of **${wiremock[SERVICE_2].resetScenarios}** in WM_RESET_SCENARIO2 variable and perform some checks later on.
For instance we can get the response:
```
"${wiremock[WM_RESET_SCENARIO2].get(response)}"
```
which returns the response body. This is the same as **${preload[WM_RESET_SCENARIO2]}** because *get(response)* can be omitted.
The **get(status)** can be used to retrieve the http response status:
 ```
 ${preload[WM_RESET_SCENARIO2].get(status)}
 ```


[![Back to the Top Of Page][upArrow]](#placeholders)

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png


