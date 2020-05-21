File Importer
=============

This tool can be used to do a single import of files or to periodically scan for files in an input folder.

Downloads
---------
Built binaries for Windows/Linux/MacOSX can be found at <https://github.com/sismics/docs/releases>

Usage
-----
```console
./docs-importer-macos (for MacOSX)
./docs-importer-linux (for Linux)
docs-importer-win.exe (for Windows)
```

A wizard will ask you for the import configuration and write it in `~/.config/preferences/com.sismics.docs.importer.pref`.
Words following a `#` in the filename will be added as tags to the document, if there is a tag with the same name on the Server.

For the next start, pass the `-d` argument to skip the wizard:
```console
./docs-importer-linux -d
```

Daemon mode
-----------
The daemon mode scan the input directory every 30 seconds for new files. Once a file is found and imported, it is **deleted**.

Build from sources
------------------
```console
npm install
npm install -g pkg
pkg .
```