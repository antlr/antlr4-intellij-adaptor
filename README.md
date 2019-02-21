# ANTLRv4 support in IntelliJ IDEs [![Build Status](https://travis-ci.org/antlr/antlr4-intellij-adaptor.svg?branch=master)](https://travis-ci.org/antlr/antlr4-intellij-adaptor) [![Maven Central](https://img.shields.io/maven-central/v/org.antlr/antlr4-intellij-adaptor.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.antlr%22%20AND%20a:%22antlr4-intellij-adaptor%22)

A library to support the use of ANTLRv4 grammars for custom languages in IntelliJ-based IDEs plug-in development.

This library has adaptors that convert ANTLR-generated parse trees into IntelliJ PSI trees.  Mostly this library is about adapting ANTLR parsers and trees, but there is considerable support to examine PSI trees derived from ANTLR parse trees. For example, if you're building a structure view for your plug-in and you want to get the list of function names you can use XPath-like specs such as `"/script/function/ID"`:

```java
Collection<? extends PsiElement> allfuncs =
    XPath.findAll(SampleLanguage.INSTANCE, tree,
                  "/script/function/ID");
```

## Using the library in your project

The library is [published on Maven Central](https://search.maven.org/search?q=a:antlr4-intellij-adaptor) which means you can download the JAR and add it to your classpath manually, or pull the dependency automatically if you are using a Gradle build:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    compile "org.antlr:antlr4-intellij-adaptor:0.1"
}
```

In Maven builds, use:

```xml
<dependency>
  <groupId>org.antlr</groupId>
  <artifactId>antlr4-intellij-adaptor</artifactId>
  <version>0.1</version>
</dependency>
```

You can now head over to the [Getting started section](https://github.com/antlr/antlr4-intellij-adaptor/wiki/Getting-started) of the wiki.

## Examples

Here is a list of known plugins that use the adaptor:

* [Sample IntelliJ plugin](https://github.com/antlr/jetbrains-plugin-sample)
* [ANTLRv4 grammar plugin](https://github.com/antlr/intellij-plugin-v4)
* [Pebble plugin](https://github.com/bjansen/pebble-intellij)

Other usages can be [found on GitHub](https://github.com/search?p=1&q=ANTLRParserAdaptor&type=Code)

## Migration from the pre-Maven version

Before 0.1, it was recommended to add this Git repo as a submodule of your own project, or to copy the source files directly.

It is now recommended to use the Maven dependency. The main **breaking change** is that the base package has been renamed from `org.antlr.jetbrains.adaptor` to `org.antlr.intellij.adaptor`
