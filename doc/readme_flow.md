[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="flow-mode"></a>
# Flow Mode Request

  * [testng.xml](#testngXml)
  * [json input file](#jsonInputFile)

Sometimes, for your tests purposes, you need to make multiple requests to the same service or to different ones in order to reach the expected results.
The **"Flow Mode"** is a test running modality designed to cover this kind of scenario that can be a bit more complex than the normal one.
In this way, you'll be able to perform sequential requests (each of which can have its own field checks), and use some specific output data retrieved from a response as input data for a next request.


<a name="testngXml"></a>
## testng.xml
If you want to execute your test with the "Flow Mode", you have to specify it in the **testng.xml** file (documentation [here](readme_singleMode.md)).
The class that, in the core module, is dedicated to handle this running mode is **com.hotels.heat.core.runner.FlowMode**.

```xml
    <test name="GOOGLE_FLOW_ENV1" enabled="true">
        <parameter name="inputJsonPath" value="/testCases/GmapFlowModeTestCases.json"/>
        <parameter name="enabledEnvironments" value="environment1"/>
        <classes>
            <class name="com.hotels.heat.core.runner.FlowMode"/>
        </classes>
    </test>
```
Now that we have created the driver for our test suite in Flow Mode, let's write down the JSON input file with all the specific test cases!

[![Back to the Top Of Page][upArrow]](#flow-mode)

<a name="jsonInputFile"></a>
## JSON input file
This is the place in which you can write your test cases in the specified modality.
The JSON schema for Flow Mode is quite similar to the other modalities, with some exceptions.
Here we should be able to perform any number of HTTP requests and put them in connection with the next ones in the same test case.
For this reason the structure of **"testCases"** attribute changes, as shown in the following example:


```json
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
            "testName": "flow mode test for new heat #1",
            "url": "/${preload[outputRspFormat]}",
            "queryParameters": {...},
            "headers": {...},
            "expects": {...},
            "outputParams" : {
                "destinationAddress" : "${path[destination_addresses[0]]}",
                "origin_addresses" : "${path[origin_addresses[0]]}"
            }
          },
          {
            "objectName": "Find_Geocode", 
            "stepNumber": "2",
            "beforeStep" : {
                "MY_VAR" : "${wiremock[WM_INSTANCE].reset}"
             }
            "webappName": "GMAPS_GEOCODE",
            "httpMethod": "GET",
            "url": "/json",
            "testName": "SVC call channel ANDROID number 2",
            "queryParameters": {
              "address": "${getStep(1).getOutputParam(origin_addresses)}", 
              "key": "${preload[GEOCODE_API_KEY]}"
            },
            "headers": {...},
            "expects": {...}
          }
        ]
      }
    ]
```
[![Back to the Top Of Page][upArrow]](#flow-mode)

### Manage more calls in the same test
As we can see in the example above, the **"e2eFlowSteps"** could contain more than one step, each of which is identified by a **"stepNumber"**, so that it can be simple to understand which step is running.

Each step produced a HTTP request to the specified **"webappName"** (that, in the [environment.properties](readme_firstConf.md) file is mapped to a given base path for all the supported environments).

Like the other running modalities, also in this case there is a **"url"** field that represents the specific endpoint to hit for that request (for further details, have a look at the [first configuration guide](readme_firstConf.md))

After each call, as for the other running modalities, the expectations block (**"expect"**) is executed and if one of them fails, the test case will be signed as 'FAILED'. 

[![Back to the Top Of Page][upArrow]](#flow-mode)

### Pass information among steps
After each request, you can add an optional **"outputParams"** attribute in order to define a set  of variables with dynamic values that could be extracted by the current response (or any of the possible [placeholders](readme_placeholders.md)) and used in one of the next ones, in the same test case.

To retrieve these variable values in next steps, we can use the placeholder **"getStep(n).getOutputParam(param_name)"** ([placeholder section](readme_placeholders.md))

In the example above, we are using the "origin_addresses" output parameter, defined in the first step, as query parameter for the request in the second step.

[![Back to the Top Of Page][upArrow]](#flow-mode)

### Before step
The **"beforeStep"** property could contain some properties which value will be resolved only before the execution (the call to "webappName") of the current step.

This is very useful used in combination with the [wiremock placeholder](readme_placeholders.md) because it's possible to reset the wiremock "cache" before the call the system under test, avoiding to add a previous dedicated step for it.  

[![Back to the Top Of Page][upArrow]](#flow-mode)

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png
