Sismics Docs
============

_Web interface_

![Web interface](http://sismics.com/docs/screenshot1.png)

_Android application_

![Android documents list](http://sismics.com/docs/android1.png) ![Android navigation](http://sismics.com/docs/android2.png) ![Android document details](http://sismics.com/docs/android3.png)

What is Docs?
---------------

Docs is an open source, lightweight document management system.

Docs is written in Java, and may be run on any operating system with Java support.

Features
--------

- Responsive user interface
- Optical character recognition
- Support image and PDF files
- Flexible search engine
- Full text search in image and PDF
- 256-bit AES encryption
- Tag system
- Multi-users ACL system
- Audit log
- Document sharing by URL
- RESTful Web API
- Fully featured Android client
- Tested to 100k documents

Download
--------

The latest release is downloadable here: <https://github.com/sismics/docs/releases> in WAR format.
You will need a Java webapp server to run it, like [Jetty](http://eclipse.org/jetty/) or [Tomcat](http://tomcat.apache.org/)

How to build Docs from the sources
----------------------------------

Prerequisites: JDK 7 with JCE, Maven 3, Tesseract 3.02

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

You will get your deployable WAR in the `docs-web/target` directory.

License
-------

Docs is released under the terms of the GPL license. See `COPYING` for more
information or see <http://opensource.org/licenses/GPL-2.0>.
