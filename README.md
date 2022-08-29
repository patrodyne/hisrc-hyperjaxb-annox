# HiSrc HyperJAXB Annox

XJC plugin to add arbitrary Java annotations to JAXB.

## Description

This repository is a fork of [jaxb2-annotate-plugin][24]. The original project was developed by the admirable
Alexey Valikov (a.k.a. [Highsource][2]). This repository contains Java projects to build Maven artifact(s)
related to the Java Architecture for XML Binding (JAXB) framework. It is one of a family of repositories
forked from [Highsource][2] that provide tools for JAXB and JPA processing. Repo and artifact names have
been changed to reflect the familial connection between the repositories and to fix a conformance issue
with the original `maven-jaxb2-plugin` name.

### List of repositories in this family

| Patrodyne                   | Highsource                  | Purpose                                                |
| --------------------------- | --------------------------- | ------------------------------------------------------ |
| [hisrc-basicjaxb-annox][11] | [annox][21]                 | Parse XML Schema to find Java annotation declarations. |
| [hisrc-basicjaxb][12]       | [jaxb2-basics][22]          | A library of XJC plugins and tools to extend JAXB.     |
| [hisrc-higherjaxb][13]      | [maven-jaxb2-plugin][23]    | Maven plugin to generated Java source from XML Schema. |
| [hisrc-hyperjaxb-annox][14] | [jaxb2-annotate-plugin][24] | XJC plugin to add arbitrary Java annotations to JAXB.  |
| [hisrc-hyperjaxb][15]       | [hyperjaxb3][25]            | Maven and XJC plugins to add JPA annotations to JAXB.  |

### Graph of repository relationships

![Patrodyne-Highsource Graph][1]

### Releases

#### GitHub Releases, Demonstrations

* TBD

#### Maven Central Repository

* [Maven Central Repository Search](https://search.maven.org/search?q=g:org.patrodyne.jvnet)
* [Maven Central Repository Index](https://repo1.maven.org/maven2/org/patrodyne/jvnet/)

### Goals

New goals for the next release are:

* Update dependencies with newer versions *including* the Jakarta namespace.
* Convert DOS line endings to Unix newlines.
* Update JUnit v4 to JUnit v5.
* Rename packages:
    * `org.jvnet.annox -> org.jvnet.basicjaxb-annox.*`
    * `org.jvnet.jaxb2_commons -> org.jvnet.basicjaxb.*`
    * `org.jvnet.mjiip -> org.jvnet.higherjaxb`
    * `org.jvnet.jaxb2.maven2 -> org.jvnet.higherjaxb.mojo`
	* `org.jvnet.hyperjaxb[23] -> org.jvnet.hyperjaxb`
* Rename namespaces:
	* `"http://annox.dev.java.net" -> "http://jvnet.org/basicjaxb/annox"`
	* `"http://jaxb2-commons.dev.java.net/basic" -> "http://jvnet.org/basicjaxb/plugin"`
	* `"http://hyperjaxb3.jvnet.org/ejb/schemas/customizations" -> "http://jvnet.org/hyperjaxb/ejb/schemas/customizations"`

### Status

#### Completed

* Update dependencies with newer versions *including* the Jakarta namespace.
* Replace 'eclipse-only' lifecyle profile with 'm2e' XML directive.

#### In Progress

* Convert DOS line endings to Unix newlines.

### Fork History

#### Version 1.1.2

* Update dependencies with newer versions *excluding* the Jakarta namespace.

#### Version 1.1.1

* Obsolete build scripts have been removed.
* New build scripts have been added.
* POMs have been refactored with renamed artifacts.
* POMs have been updated to reduce warnings and errors.
* Configured SLF4J with SimpleLogger as the log framework.
* Changes to Java sources to resolve warnings/errors.
* Verification of unit and integration tests.

<!-- References -->

  [1]: https://raw.githubusercontent.com/patrodyne/hisrc-hyperjaxb/master/etc/hisrc-repositories.svg
  [2]: https://github.com/highsource
  [11]: https://github.com/patrodyne/hisrc-basicjaxb-annox#readme
  [12]: https://github.com/patrodyne/hisrc-basicjaxb#readme
  [13]: https://github.com/patrodyne/hisrc-higherjaxb#readme
  [14]: https://github.com/patrodyne/hisrc-hyperjaxb-annox#readme
  [15]: https://github.com/patrodyne/hisrc-hyperjaxb#readme
  [21]: https://github.com/highsource/annox#readme
  [22]: https://github.com/highsource/jaxb2-basics#readme
  [23]: https://github.com/highsource/maven-jaxb2-plugin#readme
  [24]: https://github.com/highsource/jaxb2-annotate-plugin#readme
  [25]: https://github.com/highsource/hyperjaxb3#readme
