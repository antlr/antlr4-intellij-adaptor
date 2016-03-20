# ANTLR support in jetbrains IDEs

A library to support the use of ANTLR grammars for custom languages in 
jetbrains IDEs plug-in development.

This library has adaptors that convert ANTLR-generated parse trees into 
jetbrains PSI trees.  Mostly this library is about adapting ANTLR 
parsers and trees, but there is considerable support to examine PSI 
trees derived from ANTLR parse trees. For example, if you're building 
a structure view for your plug-in and you want to get the list of 
function names you can use XPath-like specs such as `"/script/function/ID"`:

```java
Collection<? extends PsiElement> allfuncs =
    XPath.findAll(SampleLanguage.INSTANCE, tree,
                  "/script/function/ID");
```

Sample plug-in that demonstrates the use of this library: [antlr/jetbrains-plugin-sample](https://github.com/antlr/jetbrains-plugin-sample).


