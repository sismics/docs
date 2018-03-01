File Importer
=============

This tool can be used to do a single import of files or to periodically scan for files in an input folder.

Requirements
------------
- NodeJS 9
- NPM

Usage
-----
```console
npm install
node main.js
```

A wizard will ask you for the import configuration and write it in `~/.config/preferences/com.sismics.docs.importer.pref`

Daemon mode
-----------
The daemon mode scan the input directory every 30 seconds for new files. Once a file is found and imported, it is **deleted**.