[![Back to Table Of Contents][leftArrow]](../readme.md)

# yaml input file

For input test file in HEAT you can use YAML file insted of Json File. You can also convert Json input files to yaml file (and back). There are some interesting feature with the use of YAML file:

  * [Comment inside the test file](#comment_inside_file)
  * [Json string in YAML file](#json_in_file)
  * [Aliases and Anchors](#aliases_anchors)


HEAT is configured to automatically detect if we are using a YAML file, so just use it in the test-ng.xml file

Example
```xml
<test name="GOOGLE_MAPS_ENV1_YML" enabled="true">
    <parameter name="inputJsonPath" value="/testCases/GmapsSingleModeTestCases.yml"/>
    <parameter name="enabledEnvironments" value="environment1"/>
    <classes>
        <class name="com.hotels.heat.core.runner.SingleMode"/>
    </classes>
</test>
```
If you want you can convert a Json file to yml file you can use this online conversion tool https://www.json2yaml.com/ (and back https://www.json2yaml.com/convert-yaml-to-json). Let's see in details, the YAML features:

<a name="comment_inside_file"></a>
## Comment inside the test file
You can add a comment with hash sign at the beginning of the comment #

```yaml
queryParameters:
  # I can add a comment
  postBody:
```

<a name="json_in_file"></a>
## Json string in YAML file
You can directly write a Json string in your YAML file, without the need of escaping the quotes.

In Json

```json
"queryParameters": {
  "postBody": "{\"custname\":\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"12:45\",\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"pizza delivery\"}"
}
```
In yaml

```yaml
postBody: '{"custname":"pippo","custemail":"pippo@test.test","delivery":"12:45","size":"large","topping":
  ["bacon","cheese"],"comment":"pizza delivery"}'
```

<a name="aliases_anchors"></a>
## Aliases and Anchors
Anchors and aliases let you identify an item with an anchor in a YAML document, and then refer to that item with an alias later in the same document. Anchors are identified by an & character, and aliases by an * character. Example

```yaml
- &hour
  description: 'json fields in output: delivery'
  actualValue: "${path[json.delivery]}"
  expectedValue: '12:45'
```
We have defined an anchor, and we can use later on in the same file:


```yaml
- description: 'json fields in output: comment'
  actualValue: "${path[json.comment]}"
  expectedValue: PIPPO
- *hour
- description: 'json fields in output: topping size'
  actualValue: "${path[json.topping.size()]}"
```

This is equivalent to

```yaml
- description: 'json fields in output: delivery'
  actualValue: "${path[json.delivery]}"
  expectedValue: '12:45'

...

- description: 'json fields in output: comment'
  actualValue: "${path[json.comment]}"
  expectedValue: PIPPO
- description: 'json fields in output: delivery'
  actualValue: "${path[json.delivery]}"
  expectedValue: '12:45'
- description: 'json fields in output: topping size'
  actualValue: "${path[json.topping.size()]}"
```



Anchor has a usefull function, when used as aliases. You can refer to a node not as a string, but as a logic node and you can modify, dinamically, subnodes. Example


```yaml
- &statusChanges
  description: result has to be OK
  actualValue: "${path[status]}"
  expectedValue: OK
- <<: *statusChanges
  expectedValue: "${preload[OK]}"
- <<: *statusChanges
  expectedValue: "${Present}"
```

This is equivalent to this not-stripped conversion

```yaml
- description: result has to be OK
  actualValue: "${path[status]}"
  expectedValue: OK
- description: result has to be OK
  actualValue: "${path[status]}"
  expectedValue: "${preload[OK]}"
- description: result has to be OK
  actualValue: "${path[status]}"
  expectedValue: "${Present}"
```

You can also use this mechanism to extend some nodes:

```yaml
# Basic function options
x-function: &function
  labels:
    function: "true"
  depends_on:
    - gateway
  networks:
    - functions
  deploy:
    placement:
      constraints:
        - 'node.platform.os == linux'
services:
  # Node.js gives OS info about the node (Host)
  nodeinfo:
    <<: *function
    image: functions/nodeinfo:latest
    environment:
      no_proxy: "gateway"
      https_proxy: $https_proxy
  # Uses `cat` to echo back response, fastest function to execute.
  echoit:
    <<: *function
    image: functions/alpine:health
    environment:
      fprocess: "cat"
      no_proxy: "gateway"
      https_proxy: $https_proxy
```



Rif.

Documentation https://medium.com/@kinghuang/docker-compose-anchors-aliases-extensions-a1e4105d70bd

Conversion pages https://www.json2yaml.com/ and https://www.json2yaml.com/convert-yaml-to-json

[leftArrow]: img/LeftArrow.png
