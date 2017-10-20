[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="compare-mode"></a>
# Compare Mode Request

  * [testng.xml](#testngXml)
  * [json input file](#jsonInputFile)

Sometimes, for your test purposes, you need to compare a response with another one, for example to check the information consistency between two endpoints.
The **"Compare Mode"** is a running modality designed to cover this kind of scenario.
In this way, you'll be able to perform two different requests and to make comparisons between the two retrieved responses.

<a name="testngXml"></a>
### testng.xml
If you want to execute your tests in "Compare Mode", you have to specify it in the **testng.xml** file (documentation [here](readme_singleMode.md)).
The class that, in the core module, is dedicated to handle this running mode is **com.hotels.restassuredframework.core.runner.CompareMode**.

```
    <test name="GOOGLE_COMPARE_ENV1" enabled="true">
        <parameter name="inputJsonPath" value="/testCases/GmapsCompareModeTestCases.json"/>
        <parameter name="enabledEnvironments" value="environment1"/>
        <classes>
            <class name="com.hotels.restassuredframework.core.runner.CompareMode"/>
        </classes>
    </test>
```
Now that we have created the driver for our test suite in Compare Mode, let's write down the JSON input file with all the specific test cases!

[![Back to the Top Of Page][upArrow]](#compare-mode)


<a name="jsonInputFile"></a>
### JSON input file
This is the place in which you can write your test cases in the specified modality.
The JSON schema for Compare Mode is quite similar to the other modalities, with some exceptions.
Here we should be able to define two comparable HTTP requests, represented by two "objects" and then to compare them in the same **"expects"** block.
For this reason the structure of **"testCases"** attribute changes, as the follow example:


```
    "testCases": [
        {
            "testId": "001",
            "testName": "Generic Compare for gmaps",
            "objectsToCompare": [
                {
                    "objectName": "gmaps distance response",
                    "webappName": "GMAPS_DISTANCE",
                    "httpMethod": "GET",
                    "url": "/json",
                    "queryParameters": {...},
                    "headers": {...}
                },
                {
                    "objectName": "geo code response",
                    "webappName": "GMAPS_GEOCODE",
                    "httpMethod": "GET",
                    "url": "/json",
                    "queryParameters": {...}
                }
            ],
            "expects": [
                {
                    "description": "check on addresses",
                    "operation": "=",
                    "actualValue": {
                        "referringObjectName": "gmaps distance response",
                        "actualValue": "${path[origin_addresses[0]]}"
                    },
                    "expectedValue": {
                        "referringObjectName": "geo code response",
                        "actualValue": "${path[results[0].formatted_address]}"
                    }
                }
            ]
        }
    ]
```

[![Back to the Top Of Page][upArrow]](#compare-mode)

### Manage two calls in the same test
As we can see in the example above, the **"objectsToCompare"** must contain two blocks identified by the **"objectName"**, so that it can be simple to understand which block we are referring to, through the checks in the test case. Each block generates a different HTTP request to a specified services, identified by a **"webappName"** (that, in the [environment.properties](readme_firstConf.md) file is mapped to a given base path for all the supported environments).
Like the other running modalities, also in this case there is a "url" field that represents the specific endpoint to hit for that request (for further details, have a look at the first [configuration guide](readme_singleMode.md)) 

After the two requests, we have a single expectations block called **"expects"** that contains one or more expectation blocks.

```json
"expects": [
                {
                    "description": "check on addresses",
                    "operation": "=",
                    "actualValue": {
                        "referringObjectName": "gmaps distance response",
                        "actualValue": "${path[origin_addresses[0]]}"
                    },
                    "expectedValue": {
                        "referringObjectName": "geo code response",
                        "actualValue": "${path[results[0].formatted_address]}"
                    }
                }
            ]
```

Each expectation block has a **description** for logging purposes, an _optional_ field **operation** (described in the [expectation section](readme_expectations.md)), an actual value and an expected one, exactly like in the other running modalities.
What is changed is that the 'actualValue' and the 'expectedValue' have a different structure, that is

* **referringObjectName** that is the identification of the block whose response we are analysing to retrieve the value to use for the final check
* **actualValue** field that has the same structure explained in the [expectation section](readme_expectations.md).

In the example above, the actual value will be the `${path[origin_addresses[0]]}` from the response coming from the object whose 'referringObjectName' is 'gmaps distance response', and the expected value will be the `${path[results[0].formatted_address]}` from the response coming from the object whose 'referringObjectName' is 'geo code response'.

 
 [![Back to the Top Of Page][upArrow]](#compare-mode)
 
[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png
