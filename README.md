Wicket-Gatling Integration
==========================

Load testing Wicket-based web applications with the Gatling framework is not easy
when using just out-of-the-box Gatling. This is because stateful Wicket pages
rewrite URIs every time a page is requested to maintain proper state of the page. They
may also use AJAX links, or other tricks to get events back to the components.

Unfortunately Gatling can only comfortably deal with static URIs to build requests, for example:

```scala
val scenario = scenario("Test")
    .exec(http("Homepage").get("/"))
    .exec(http("Profile").get("/profile"))
```

This would usually not work in Wicket, since the link to the profile would not be known at
the time of writing this scenario. The link itself would be only contained in the requested
homepage at runtime.

This extension to Gatling makes it possible to write this test the following way:

```scala
val scenario = scenario("Test")
    .exec(http("Homepage").get("/"))
    .exec(http("Profile").get(wicketLinkUnder("profile-link")))
```

This library enables the scenario to extract relevant dynamically generated URIs automatically 
from the previously requested page and use it in the next request.

## Quickstart

### Getting the library

If you are using Maven, add this dependency to your pom file:

```xml
<dependency>
   <groupId>com.vanillasource.wicket-gatling</groupId>
   <artifactId>wicket-gatling</artifactId>
   <version>1.0.1</version>
</dependency>
```

If you are using sbt:

```sbt
libraryDependencies += "com.vanillasource.wicket-gatling" % "wicket-gatling" % "1.0.1"

```

### Setting up Wicket

This library selects Wicket URIs out of the response based on the *wicketpath* attribute set in the
generated HTML. This is not on by default, so you have to enable it for development environments,
by putting the following code in your Application class:

```java
if (getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) {
   getDebugSettings().setOutputComponentPath(true);
}
```

### Setting up the Gatling Configuration

To enable parsing Wicket URIs from the response HTML the `enableWicketTargets()` method needs to be called on the HTTP Configuration:

```scala
import com.vanillasource.wicket.gatling.HttpConfig._

val httpConfig = http
    .baseURL(url)
    .enableWicketTargets()
    ...
```

It is important that the `baseURL()` is set before the `enableWicketTargets()` is called.

## Usage

After Wicket targets are enabled, you can use the following methods to retrieve Wicket URIs to make a request:

 * `wicketLinksUnder(path_fragment)`: Returns a list of links for the given wicket path.
 * `wicketLinkUnder(path_fragment)`: Expects and returns a single URI under the given path.
 * `wicketFormsUnder(path_fragment)`: Returns a list of form URIs for the given wicket path.
 * `wicketFormUnder(path_fragment)`: Expects and returns a single URI under the given path.

Currently supported link and form types:

 * The `Link` component on any tag (support for both direct *href*, and *onclick* variant)
 * `AjaxLink`s
 * Normal Forms with POST to the *action* attribute (can directly submit Form)
 * Forms with `AjaxButton`, in which case both the Form and the Button can be used to submit

### Getting exactly one URI

Methods `wicketLinkUnder()` and `wicketFormUnder()` can be both directly used in `get()` and `post()` methods:

```scala
val scenario = scenario("Test")
    .exec(http("Homepage").get("/"))
    .exec(http("Profile").get(wicketLinkUnder("profile-link")))
```

If there is not exactly one URI found, this will cause a Gatling *KO* for the request.

### Selecting from multiple URIs

Methods `wicketLinksUnder()` and `wicketFormsUnder()` both return a list of URIs from which a single URI needs to be selected first, this can be done with the helper method `selectUri()` the following way:

```scala
...
   .exec(http("Page").get(wicketLinksUnder("list-item").selectUri(
       list => list(rnd.nextInt(list.size)))
...
```
The `selectUri()` method takes a function `List[String] => String`.

### Conditional execution

The `wicketUriExists()` method can be used to do conditional executions in Gatling with the `doIf` execution instruction:

```scala
...
   .doIf(wicketUriExists("next-page")) {
   }
...
```

## Wicket path matching

All methods that take a path fragment match this path fragment against the full wicket path found in the HTML. Wicket paths are build from the *id*s of the wicket components.

A full wicket path matches a path fragment if the path fragment is a subset of the full wicket path in the same order. For example:

 * `wicketLinksUnder()`: This call with an empty path fragment will return **all** links from the previous response.
 * `wicketLinksUnder("next-page")`: Will return every link found under any path that has a component with id *next-page* in it.
 * `wicketLinksUnder("content", "person-list", "next-page")`: With 3 components given, this will return all links found under the component *next-page* that has to be under a component with id *person-list*, that has to be under a component with id *content*.
 
It is possible to use Gatling expressions in the path fragment like this:

```scala
   .exec(http("Current Details")
       .get(wicketLinkUnder("person-table", "detail-link${currentPersonIndex}")))
```

This will substitute the session variable *currentPersonIndex* into the path fragment.

