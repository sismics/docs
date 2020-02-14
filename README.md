<h3 align="center">
  <img src="https://teedy.io/img/github-title.png" alt="Teedy" width=500 />
</h3>

[![Twitter: @teedyio](https://img.shields.io/badge/contact-@teedyio-blue.svg?style=flat)](https://twitter.com/teedyio)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-blue.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![Build Status](https://secure.travis-ci.org/sismics/docs.png)](http://travis-ci.org/sismics/docs)

Teedy is an open source, lightweight document management system for individuals and businesses.

<hr />
<h2 align="center">
  ✨ <a href="https://github.com/users/jendib/sponsorship">Sponsor this project if you use and appreciate it!</a> ✨
</h2>
<hr />

![New!](https://teedy.io/img/laptop-demo.png?20180301)

Demo
----

A demo is available at [demo.teedy.io](https://demo.teedy.io)
- Guest login is enabled with read access on all documents
- "admin" login with "admin" password
- "demo" login with "password" password 

Features
--------

- Responsive user interface
- Optical character recognition
- Support image, PDF, ODT, DOCX, PPTX files
- Video file support
- Flexible search engine with suggestions and highlighting
- Full text search in all supported files
- All [Dublin Core](http://dublincore.org/) metadata
- Custom user-defined metadata ![New!](https://www.sismics.com/public/img/new.png)
- Workflow system ![New!](https://www.sismics.com/public/img/new.png)
- 256-bit AES encryption of stored files
- File versioning ![New!](https://www.sismics.com/public/img/new.png)
- Tag system with nesting
- Import document from email (EML format)
- Automatic inbox scanning and importing
- User/group permission system
- 2-factor authentication
- Hierarchical groups
- Audit log
- Comments
- Storage quota per user
- Document sharing by URL
- RESTful Web API
- Webhooks to trigger external service
- Fully featured Android client
- [Bulk files importer](https://github.com/sismics/docs/tree/master/docs-importer) (single or scan mode)
- Tested to one million documents

Install with Docker
-------------------

A preconfigured Docker image is available, including OCR and media conversion tools, listening on port 8080. The database is an embedded H2 database but PostgreSQL is also supported for more performance.

**The default admin password is "admin". Don't forget to change it before going to production.**
- Master branch, can be unstable. Not recommended for production use: `sismics/docs:latest`
- Latest stable version: `sismics/docs:v1.7`

The data directory is `/data`. Don't forget to mount a volume on it.

To build external URL, the server is expecting a `DOCS_BASE_URL` environment variable (for example https://teedy.mycompany.com)

Manual installation
-------------------

#### Requirements
- Java 8 with the [Java Cryptography Extension](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
- Tesseract 3 or 4 for OCR
- ffmpeg for video thumbnails
- mediainfo for video metadata extraction
- A webapp server like [Jetty](http://eclipse.org/jetty/) or [Tomcat](http://tomcat.apache.org/)

#### Download
The latest release is downloadable here: <https://github.com/sismics/docs/releases> in WAR format. 
**The default admin password is "admin". Don't forget to change it before going to production.**

How to build Teedy from the sources
----------------------------------

Prerequisites: JDK 8 with JCE, Maven 3, NPM, Grunt, Tesseract 3 or 4

Teedy is organized in several Maven modules:

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

License
-------

Teedy is released under the terms of the GPL license. See `COPYING` for more
information or see <http://opensource.org/licenses/GPL-2.0>.
