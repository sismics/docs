Sismics Docs
============

![](http://www.sismics.com/docs/img/docs.jpg)

What is Docs?
---------------

Docs is an open source, lightweight document management system.

Docs is written in Java, and may be run on any operating system with Java support.

Features
--------

- Responsive user interface
- Optical characted recognition
- Support image and PDF files
- Flexible search engine
- Full text search in image and PDF
- Tag system
- Multi-users
- Document sharing
- RESTful Web API

License
-------

Docs is released under the terms of the GPL license. See `COPYING` for more
information or see <http://opensource.org/licenses/GPL-2.0>.

How to build Docs from the sources
----------------------------------

Prerequisites: JDK 7, Maven 3, Tesseract 3.02

Docs is organized in several Maven modules:

  - docs-parent
  - docs-core
  - docs-web
  - docs-web-common

First off, clone the repository: `git clone git://github.com/sismics/docs.git`
or download the sources from GitHub.

#### Launch the build

From the `docs-parent` directory:

    mvn -Pinit validate -N
    mvn clean -DskipTests install

#### Run a stand-alone version

From the `docs-web` directory:

    mvn jetty:run

#### Build a .war to deploy to your servlet container

From the `docs-web` directory:

    mvn -Pprod -DskipTests clean install

You will get your deployable WAR in the `target` directory.
