[![Back to Table Of Contents][leftArrow]](../readme.md)

<a name="external_modules"></a>
# HEAT external Modules

  * [Initial configuration](#initialConf)
  * [Placeholder Module](#placeholderModuleProvider)
  * [Placeholder Module Provider](#placeholderModule)
  * [Use your Placeholder in a HEAT test](#placeholderUsage)

HEAT is an extendible framework that allows users to define their own custom modules.
In particular, the current version includes the possibility to create new **custom placeholders** as **HeatModule**.

Therefore, besides the core placeholders (explained in the [placeholder section](readme_placeholders.md)), your own placeholder can have a customized name and structured object in output with data organized as you prefer.

```xml
<!-- core placeholder that generates a date -->
"CHECK_IN": "${TODAY+100_YYYY-MM-dd}",
...

<!-- customized placeholder that generates a custom object -->
"CUST_OBJ": "${MY_PLACEHOLDER}",
...
```

<a name="initialConf"></a>
## Initial configuration

Usually, the only suggested dependency to use HEAT is "heat-bundle", that includes all the useful dependencies for use or extend the framework. 
Anyway, if you want use the minimal set of dependencies to define your own Placeholder, you could use the following subset of dependencies as an alternative to "heat-bundle":

```xml
<!-- my pom.xml --> 
...
    <dependencies>
        <dependency>
            <groupId>com.hotels</groupId>
            <artifactId>heat-module-support</artifactId>
            <version>1.0.X</version>
        </dependency>
        <dependency>
            <groupId>com.hotels</groupId>
            <artifactId>heat-core-utils</artifactId>
            <version>1.0.X</version>
        </dependency>
    ...
    </dependencies>
```

[![Back to the Top Of Page][upArrow]](#external_modules)

<a name="placeholderModuleProvider"></a>
## Placeholder Module Provider

First of all, you have to decide the name of your placeholder and associate it to a specific HEAT module that could manage it.

If you would like to create an external module that is able to retrieve information about a particular customer, starting from his ID, we could define the following syntax for the corrispondent placeholder:

```xml
CUSTOMERDATA(ID)
```

Now we have to associate this placeholder string to a particulare HEAT module. In order to do this, we need to implement our **HeatPlaceholderModuleProvider**:

```java
public class CustomerDataPlaceholderModuleProvider implements HeatPlaceholderModuleProvider {
```

This interface needs to implement two methods.
The first one **getHandledPlaceholders()** is needed in order to associate the placeholder strings to the HEAT module. The second one, **getModuleInstance()**, has the responsability to provide an instance of HeatPlaceholderModule that is able to manage the placeholders.

For example:

```java
    @Override
    public List<String> getHandledPlaceholders() {
        List<String> listPlaceholders = new ArrayList<>();
        listPlaceholders.add("CUSTOMERDATA("); //the placeholder starts with
        return listPlaceholders;
    }

    @Override
    public HeatPlaceholderModule getModuleInstance() {
        return new CustomerDataPlaceholderModule();
    }
```

In other words, you are creating a sort of virtual map between a custom placeholder and the Java class (in the external module) that is able to handle it. This map is read at heat test startup and only if a custom placeholder is used, the associated class is instantiate, otherwise not. This is a good performance advantage, because it avoids to handle unuseful pieces of code.

[![Back to the Top Of Page][upArrow]](#external_modules)

<a name="placeholderModule"></a>
## Placeholder Module

Now, you actually have to implement the **HeatPlaceholderModule** (the specific **HeatModule** interface for placeholder extensions) provided by **HeatPlaceholderModuleProvider**.

```java
public class CustomerDataPlaceholderModule implements HeatPlaceholderModule {
    ...
}
```

This interface needs the definition of the **process()** method, that accepts the placeholder String and the information related to the current test, and returns a Map with all the retrieved/calculated information for you purposes.


```java
Map<String, String> process(String placeholder, HeatTestDetails testDetails);
```

Here you should put the logic to retrieve the correct value for your placeholder.

For example, for our "CUSTOMERDATA" placeholder, you could implement a logic for retrieve the information from an external service and inject these data in the output object:

```java
    @Override
    public Map<String, String> process(String placeholder, HeatTestDetails testDetails) {
        Map<String, String> processedObj = new HashMap<>();
        
        String cId = getCustomerIdFromPlaceholder(placeholder);
        CustomerData cd = retieveCustomerDataById(cId);

        processedObj.put("ID", cId);
        processedObj.put("name", cd.getName());
        processedObj.put("surname", cd.getSurname);
        processedObj.put("CF", cd.getCF());

        return processedObj;
    }
```



[![Back to the Top Of Page][upArrow]](#external_modules)

<a name="placeholderUsage"></a>
## Use your Placeholder in a HEAT test
In order to use your new HeatPlaceholderModule, you should have defined this module in you current project or, a best practice to maximize reusability, you should define your HeatModule as separated artifact and then include it as dependency in your project.
 
```xml
    <dependency>
        <groupId>my.group-id</groupId>
        <artifactId>heat-customer-data-module</artifactId>
        <version>X.Y.Z</version>
    </dependency>
``` 
 
Within the JSON input file, you could now use this new placeholder in this way, for example:

```json
        "preloadVariables": {
            "CUSTDATA": "${CUSTOMERDATA(123)}"
        },
        ....
        "queryParameters": {
            "customer-id"     : "${preload[CUSTDATA].get(ID)",
            "customer-name"   : "${preload[CUSTDATA].get(name)",
            "customer-surname": "${preload[CUSTDATA].get(surname)",
            "customer-cf"     : "${preload[CUSTDATA].get(CF)"
        },
        ...
```

In the example above, the external module is called only once in the 'preloadVariables' section handling and it retrieves a set of information (the map quoted in the [previous](#placeholderModule) paragraph). Then the user is able to retrieve any single piece of information coming from that set, simply by accessing to the variable loaded.

This is a performance advantage! Let's think, for example, of an external module that, to retrieve some information, makes a request to an external REST service and extract data from a single response. Not to aggregate these information would mean to make as many requests (equal among each others) as how many data we want to retrieve. In the example above we want four data (customer-id, customer-name, customer-surname, customer-cf) and with a 'single-data approach' we would have done four different requests to an external service. Aggregating them, we can make the request only once. 


[![Back to the Top Of Page][upArrow]](#external_modules)

[upArrow]: img/UpArrow.png
[leftArrow]: img/LeftArrow.png
