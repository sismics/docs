<h3 align="center">
  <img src="https://www.sismicsdocs.com/img/github-title.png" alt="Sismics Docs" width=500 />
</h3>

[![Twitter: @sismicsdocs](https://img.shields.io/badge/contact-@sismicsdocs-blue.svg?style=flat)](https://twitter.com/sismicsdocs)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![Build Status](https://secure.travis-ci.org/sismics/docs.png)](http://travis-ci.org/sismics/docs)
[![Read the Docs](https://img.shields.io/readthedocs/pip.svg)](https://demo.sismicsdocs.com/apidoc/)

Docs is an open source, lightweight document management system for individuals and businesses.

**Discuss it on [Product Hunt](https://www.producthunt.com/posts/sismics-docs) 🦄**

<hr />
<h2 align="center">
  ✨ We just launched a Cloud version of Sismics Docs! Head to <a href="https://www.sismicsdocs.com/">sismicsdocs.com</a> for more informations ✨
</h2>
<hr />

![New!](https://www.sismicsdocs.com/img/laptop-demo.png?20180301)

Demo
----

A demo is available at [demo.sismicsdocs.com](https://demo.sismicsdocs.com)
- Guest login is enabled with read access on all documents
- "admin" login with "admin" password
- "demo" login with "password" password 

Features
--------

- Responsive user interface
- Optical character recognition
- Support image, PDF, ODT and DOCX files
- Video file support ![New!](https://www.sismics.com/public/img/new.png)
- Flexible search engine
- Full text search in all supported files
- All [Dublin Core](http://dublincore.org/) metadata
- Workflow system ![New!](https://www.sismics.com/public/img/new.png)
- 256-bit AES encryption of stored files
- Tag system with nesting
- Import document from email (EML format) ![New!](https://www.sismics.com/public/img/new.png)
- Automatic inbox scanning and importing ![New!](https://www.sismics.com/public/img/new.png)
- User/group permission system
- 2-factor authentication
- Hierarchical groups
- Audit log
- Comments
- Storage quota per user
- Document sharing by URL
- RESTful Web API
- Fully featured Android client
- [Bulk files importer](https://github.com/sismics/docs/tree/master/docs-importer) (single or scan mode) ![New!](https://www.sismics.com/public/img/new.png)
- Tested to 100k documents

Install with Docker
-------------------

From a Docker host, run this command to download and install Sismics Docs. The server will run on <http://[your-docker-host-ip]:8100>.
The default admin password is "admin". Don't forget to change it before going to production.

    docker run --rm --name sismics_docs_latest -d -p 8100:8080 -v sismics_docs_latest:/data sismics/docs:latest

Manual installation
-------------------

#### Requirements
- Java 7 with the [Java Cryptography Extension](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
- Tesseract 3.02 for OCR
- ffmpeg for video thumbnails
- mediainfo for video metadata extraction
- A webapp server like [Jetty](http://eclipse.org/jetty/) or [Tomcat](http://tomcat.apache.org/)

#### Download
The latest release is downloadable here: <https://github.com/sismics/docs/releases> in WAR format. 
**The default admin password is "admin". Don't forget to change it before going to production.**

How to build Docs from the sources
----------------------------------

Prerequisites: JDK 7 with JCE, Maven 3, Tesseract 3.02

Docs is organized in several Maven modules:

  - docs-core
  - docs-web
  - docs-web-common

First off, clone the repository: `git clone git://github.com/sismics/docs.git`
or download the sources from GitHub.

#### Launch the build

From the root directory:

    mvn clean -DskipTests install

#### Run a stand-alone version

From the `docs-web` directory:

    mvn jetty:run

#### Build a .war to deploy to your servlet container

From the `docs-web` directory:

    mvn -Pprod -DskipTests clean install

You will get your deployable WAR in the `docs-web/target` directory.

Contributing
------------

All contributions are more than welcomed. Contributions may close an issue, fix a bug (reported or not reported), improve the existing code, add new feature, and so on.

The `master` branch is the default and base branch for the project. It is used for development and all Pull Requests should go there.


Community
---------

Get updates on Sismics Docs' development and chat with the project maintainers:

- Follow [@sismicsdocs on Twitter](https://twitter.com/sismicsdocs)
- Read and subscribe to [The Official Sismics Docs Blog](https://blog.sismicsdocs.com/)
- Check the [Official Website](https://www.sismicsdocs.com)
- Join us [on Facebook](https://www.facebook.com/sismicsdocs)

License
-------

Docs is released under the terms of the GPL license. See `COPYING` for more
information or see <http://opensource.org/licenses/GPL-2.0>.
