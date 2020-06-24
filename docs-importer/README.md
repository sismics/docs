# File Importer

This tool can be used to do a single import of files or to periodically scan for files in an input folder.

## Downloads

Built binaries for Windows/Linux/MacOSX can be found at <https://github.com/sismics/docs/releases>

## Usage

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

## Daemon mode

The daemon mode scan the input directory every 30 seconds for new files. Once a file is found and imported, it is **deleted**. You can set a `copyFolder` to copy the file to before deletion.

## Docker

The docker image needs a volume mounted from a previously generated preference file at `/root/.config/preferences/com.sismics.docs.importer.pref`. The container will start the importer in daemon mode. It will look for files in `/import`.
Example usage:

```
docker run --name teedy-import -d -v /path/to/preferencefile:/root/.config/preferences/com.sismics.docs.importer.pref -v /path/to/import/folder:/import sismics/docs-importer:latest
```
### Environment variables
Instead of mounting the preferences file, the options can also be set by setting the environment variables `TEEDY_TAG`, `TEEDY_ADDTAGS`, `TEEDY_LANG`, `TEEDY_COPYFOLDER`, `TEEDY_FILEFILTER`, `TEEDY_URL`, `TEEDY_USERNAME` and `TEEDY_PASSWORD`.
The latter three have to be set for the importer to work. The value of `TEEDY_TAG` has to be set to the UUID of the tag, not the name (The UUID can be found by visiting `baseUrl/api/tag/list` in your browser).
Example usage:

```
docker run --name teedy-import -d -e TEEDY_TAG=2071fdf7-0e26-409d-b53d-f25823a5eb9e -e TEEDY_ADDTAGS=false -e TEEDY_LANG=eng -e TEEDY_URL='http://teedy.example.com:port' -e TEEDY_USERNAME=username -e TEEDY_PASSWORD=superSecretPassword -v /path/to/import/folder:/import sismics/docs-importer:latest
```

## Build from sources

```console
npm install
npm install -g pkg
pkg .
```
