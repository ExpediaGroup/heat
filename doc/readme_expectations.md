[![Back to Table Of Contents][leftArrow]](../readme.md)

<a id="expectations"></a>
# Expectations

  * [A particular element in the json response is equal to a value](#equalToValue)
  * [Check the presence or the absence of a specific field in a response](#presenceOfField)
  * [Check if a string contains another string or not](#containsString)
  * [Check if an array in the response has a specific size](#arraySize)
  * [Regular expressions (regexp and regexpToMatch)](#regularExpression)
  * [Checks on the number of occurrences of a string in another string](#occurrencesOfString)
  * [Condition on the execution of a check](#conditionsOnExecution)
 
Each run modality aim is to check some expectations, starting from the simplest ones (i.e. check if a string is equal to another one) to something definitely harder (regular expressions, particular path extractions...). In this section we will analyse all the supported ways to write them.

All the expectation elements are made of the following base elements:

* **"description"**: useful for logging, is the string that describes what we are going to verify
* **"actualValue"**: is the value to check. It can be a given string, something extracted by a response, the result of a regular expression extraction, and so on...
* **"expectedValue"**: is the value that we expect to be equal to the actual value
* **"operation"** (_optional_): is the operation to do between `actualValue` and `expectedValue`. By default the operation is the equality `=`.

This is a quick reference to all the available expectations[^1]:

|     Expectation    | Example                                                                                                          |                                         Short description                                         |
|:------------------:|------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------:|
|      equality      | `"actualValue": "...", "expectedValue": "..."`                                                                    | True if "actualValue" and "expectedValue" are equal                                               |
| comparison         | `"actualValue": "...", "operation": ">","expectedValue": "1"`                                                      | True if operation (`<, <=,  >, >=`) applied to actualValue and expectedValue is comply             |
|      presence      | `"actualValue": "${path[...]}", "expectedValue": "${Present}"`                                                     | True if the field in "actualValue" is present (even if value is empty)                            |
|       absence      | `"actualValue": "${path[...]}", "expectedValue": "${NotPresent}"`                                                  | True if the field in "actualValue" is not present                                                 |
|      contains      | `"operation": "contains", "expectedValue": ["...","..."]`                                                          | True if the resolved value in actualValue contains all values inside square brackets              |
| regexp | `"actualValue": {     "regexp" : "...",     "stringToParse" : "..." },  "expectedValue" : "..."`                   |  The result of applying of "regexp" to "stringToParse" will be put into "actualValue". Then, we normally compare "actualValue" with "expectedValue"                 |
| regexpToMatch        | `"actualValue":{     "regexpToMatch" : "...",     "stringToParse" : "..." }, "expectedValue" : "true"`                    | True if "stringToParse" matches exactly with "regexpToMatch" pattern              |
| count occurences   | `"actualValue":{     "occurrenceOf" : "...",     "stringToParse":"..." }, "operation" : "=", "expectedValue": "1"` | True if the number of "occurenceOf" in "stringToParse" is equal (>, >=, <, <=) to "expectedValue" |
| precondition       | `{     "condition": [         {...},         {...}     ],     "actualValue": "...",     "expectedValue": "1" }`    | The expectation block is performed only if "condition" block is all true.                         |

[![Back to the Top Of Page][upArrow]](#expectations)

<a name="equalToValue"></a>
## A particular element in the json response is equal to a value
For example we have a json response like 

```json
{
	"status":"OK",
	"date":"2017-07-19"
}
```

To verify if the field "status" in the response is equal to "OK", we can write the expectation as follows:

```json
{
    "description": "status field has to be equal to OK",
    "actualValue": "${path[status]}",
    "expectedValue": "OK"
}
```

To verify if the field "date" in the response is equal to today date, we can write the expectation as follows:

```json
{
    "description": "date field has to be equal to today date",
    "actualValue": "${path[date]}",
    "expectedValue": "${TODAY_YYYY-MM-dd}"
}
```


[![Back to the Top Of Page][upArrow]](#expectations)

<a name="presenceOfField"></a>
## Check the presence or the absence of a specific field in a response
For example we have a json response like 

```json
{
	"status":"OK",
	"date":"2017-07-19"
}
```
To verify if the field "status" in the response is present, we can write the expectation as follows:

```json
{
    "description": "status field has to be present in the response",
    "actualValue": "${path[status]}",
    "expectedValue": "${Present}"
}
```
To verify if the field "mode" in the response is NOT present, we can write the expectation as follows:

```json
{
    "description": "mode field has not to be present in the response",
    "actualValue": "${path[mode]}",
    "expectedValue": "${NotPresent}"
}
```

[![Back to the Top Of Page][upArrow]](#expectations)

<a name="containsString"></a>
## Check if a string contains another string or not
For example we have a json response like 

```json
{
	"status":"OK",
	"date":"2017-07-19",
	"mode":"the party will take place tomorrow at 9:00"
}
```
To verify if the field "mode" in the response contains the string 'party', we can write the expectation as follows:

```json
{
    "description": "mode field has to contain the string 'party'",
    "actualValue": "${path[mode]}",
    "operation": "contains",
    "expectedValue": ["party"]
}
```

**NOTE:** When you put square brackets in **"expectedValue"** the default operation will be **"contains"**, therefore you may omit to declare it. So, the following expectation is equivalent to the previous:

```json
{
    "description": "mode field has to contain the string 'party'",
    "actualValue": "${path[mode]}",
    "expectedValue": ["party"]
}
```

To verify if the field "mode" in the response contains the strings 'party' and 'tomorrow', we can write the expectation as follows:

```json
{
    "description": "mode field has to be contain 'party' and 'tomorrow' strings",
    "actualValue": "${path[mode]}",
    "operation": "contains",
    "expectedValue": ["party","tomorrow"]
}
```

To verify if the field "mode" in the response does not contain the strings 'today', we can write the expectation as follows:

```json
{
    "description": "mode field has not to contain the string 'today'",
    "actualValue": "${path[mode]}",
    "operation": "not contains",
    "expectedValue": ["today"]
}
```

[![Back to the Top Of Page][upArrow]](#expectations)

<a name="arraySize"></a>
## Check if an array in the response has a specific size

For example we have a json response like 

```json
{
	"status":"OK",
	"date":"2017-07-19",
	"mode":"the party will take place tomorrow at 9:00",
	"partecipants":[
		"John":"confirmed",
		"Lara":"confirmed",
		"Boe":"not confirmed",
		"Peter":"confirmed"
	]
}
```
To verify if the array "partecipants" in the response contains exactly 4 elements, we can write the expectation as follows:

```json
{
    "description": "partecipants array contains 4 elements",
    "actualValue": "${path[partecipants].size()}",
    "operation": "=",
    "expectedValue": "4"
}
```
**NOTE:** When you put a simple string in **"expectedValue"** the default operation will be **"="**, therefore you may omit to declare it. So, the following expectation is equivalent to the previous:

```json
{
    "description": "partecipants array contains 4 elements",
    "actualValue": "${path[partecipants].size()}",
    "expectedValue": "4"
}
```

To verify if the array "partecipants" in the response contains more than 1 element, we can write the expectation as follows:

```json
{
    "description": "partecipants array contains more than 1 element",
    "actualValue": "${path[partecipants].size()}",
    "operation": ">",
    "expectedValue": "1"
}
```
In a similar way, within **"operation"** value you could also use the other comparison operators like: <, <=, >=.

[![Back to the Top Of Page][upArrow]](#expectations)

<a name="regularExpression"></a>
## Regular expressions (regexp and regexpToMatch)
For example we have a json response like 

```json
{
	"status":"OK",
	"date":"2017-07-19",
	"mode":"the party will take place tomorrow at 9:00",
	"partecipants":[
		"John":"confirmed",
		"Lara":"confirmed",
		"Boe":"not confirmed",
		"Peter":"confirmed"
	],
	"ticketSerialNumber":"PARTY_101_NUMBER"
}
```
To verify if the string "ticketSerialNumber" in the response has the serial number '101', we can use a regular expression and write the expectation as follows:

```json
{
    "description": "ticket serial number verification",
	"actualValue": {
            "regexp":"PARTY_(.*?)_NUMBER",
            "stringToParse":"${path[ticketSerialNumber]}"
        },
    "expectedValue": "101"
}
```
where 'regexp' contains the regular expression to apply to the string to analyse.

In this way, the object 'actualValue' will be solved first, and the output of this elaboration will be compared to the 'expectedValue'.

This structure can be used, in the same way, to the 'expectedValue' or on both 'actualValue' and 'expectedValue'

```json
{
    "description": "ticket serial number verification",
	"actualValue": "101",
    "expectedValue": {
            "regexp":"PARTY_(.*?)_NUMBER",
            "stringToParse":"${path[ticketSerialNumber]}"
        }
}
```

```json
{
    "description": "ticket serial number verification",
	"actualValue": {
            "regexp":"PARTY_(.*?)_NUMBER",
            "stringToParse":"${path[ticketSerialNumber]}"
        },
    "expectedValue": {
            "regexp":"PARTY_(.*?)_NUMBER",
            "stringToParse":"${path[ticketSerialNumber]}"
        }
}
```

We can also verify if a a string matches with a regular expression. In this case we will use a similar structure but with 'regexpToMatch' instead of 'regexp'. And we can expect if it's matched (true) or not (false):

```json
{
    "description": "ticket serial number verification",
	"actualValue": {
            "regexpToMatch":"PARTY_(.*?)_NUMBER",
            "stringToParse":"PARTY_101_NUMBER"
        },
    "expectedValue": "true"
}
```

```json
{
    "description": "ticket serial number verification",
	"actualValue": {
            "regexpToMatch":"PARTY_(.*?)_NUMBER",
            "stringToParse":"BLA BLA BLA"
        },
    "expectedValue": "false"
}
```

[![Back to the Top Of Page][upArrow]](#expectations)

<a name="occurrencesOfString"></a>
## Checks on the number of occurrences of a string in another string

For example we have a json response like 

```json
{
	"status":"OK",
	"date":"2017-07-19",
	"mode":"the party will take place tomorrow at 9:00",
	"partecipants":[
		"John":"confirmed",
		"Lara":"confirmed",
		"Boe":"not confirmed",
		"Peter":"confirmed"
	],
	"ticketSerialNumber":"PARTY_101_NUMBER"
}
```
To verify if the string "ticketSerialNumber" in the response contains only once the string 'PARTY', we can write the expectation as follows:

```json
{
	"description": "ticket serial number contains 'PARTY' only once",
	"actualValue": {
            "occurrenceOf":"PARTY",
            "stringToParse":"${path[ticketSerialNumber]}"
        },
    "operation": "=",
	"expectedValue": "1"
}
```
**NOTE:** When you put a simple string in **"expectedValue"** the default operation will be **"="**, therefore you may omit to declare it. So, the following expectation is equivalent to the previous:

```json
{
	"description": "ticket serial number contains 'PARTY' only once",
	"actualValue": {
            "occurrenceOf":"PARTY",
            "stringToParse":"${path[ticketSerialNumber]}"
        },
	"expectedValue": "1"
}
```

In a similar way, within **"operation"** value you could also use the other comparison operators like: <, <=, >=.

This structure can be applied also to **'expectedValue'** as just said for the regular expressions.[^1]

[![Back to the Top Of Page][upArrow]](#expectations)

<a name="conditionsOnExecution"></a>
## Condition on the execution of a check
Sometimes we have to execute a check only some other checks are verified.

For example we have a json response like 

```json
{
	"status":"OK",
	"date":"2017-07-19",
	"mode":"the party will take place tomorrow at 9:00",
	"partecipants":[
		"John":"confirmed",
		"Lara":"confirmed",
		"Boe":"not confirmed",
		"Peter":"confirmed"
	],
	"ticketSerialNumber":"PARTY_101_NUMBER"
}
```
Let's suppose that we have to check the number of the party partecipants only if 'status' is OK and the 'mode' field contains 'tomorrow' string. We can add the element `condition` that is an array of expectations having the same structure as the blocks analysed up to now.

```json
{
	"condition": [
        {
            "description": "if the status is OK",
            "actualValue": "${path[status]}",
            "expectedValue": "OK"
        },
        {
    		  "description": "if the mode field contains 'tomorrow' string",
            "actualValue": "${path[mode]}",
            "expectedValue": ["tomorrow"]
        }
    ],
	"description": "ticket serial number contains 'PARTY' only once",
	"actualValue": "${path[partecipants.size()]}",
	"expectedValue": "4"
}
```
In this way, the condition blocks will be executed first, and only if all of them are successful the proper check (the number of partecipants) will be done, otherwise the block will be skipped and not signed as 'failed'.

[![Back to the Top Of Page][upArrow]](#expectations)

[^1]: if you have doubts on how to navigate a json, please refer to [placeholders](readme_placeholders.md) section.

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png