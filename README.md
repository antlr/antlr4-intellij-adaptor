# ANTLR support in jetbrains IDEs

A library to support the use of ANTLR grammars in jetbrains IDEs for building custom languages.

This library has adaptors that convert ANTLR-generated parse trees into jetbrains PSI trees.  Mostly this library is about adapting ANTLR parsers and trees, but there is considerable support to examine PSI trees derived from ANTLR parse trees. For example, if you're building a structure view for your plug-in and you want to get the list of function names you can use XPath-like specs such as `"/script/function/ID"`:

```java
Collection<? extends PsiElement> allfuncs =
    XPath.findAll(SampleLanguage.INSTANCE, tree,
                  "/script/function/ID");
```

I have made a sample plug-in that demonstrates the use of this library: [antlr/jetbrains-plugin-sample](https://github.com/antlr/jetbrains-plugin-sample).

I can't use mvn to build and publish this library (see [pom.xml](pom.xml) but you can use a [git submodule](https://git-scm.com/docs/git-submodule) to clone this library underneath your plug-in root directory as, perhaps, `adaptor` directory:

```bash
$ cd myplugin
$ git submodule add git@github.com:antlr/jetbrains.git adaptor
$ ls adaptor
./                .git              LICENSE           contributors.txt  pom.xml
../               .gitignore        README.md         doc/              src/
```

Then tell your plug-in build to look in that directory for source code. This is what the sample plug-in does.

