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

The daemon mode scan the input directory every 30 seconds for new files. Once a file is found and imported, it is **deleted**.

## Docker

The docker image needs a volume mount of a previously generated preference file to `/root/.config/preferences/com.sismics.docs.importer.pref`. The container will start the importer in daemon mode. It will look for files in `/import`.
Example usage:

```docker
docker build -t teedy-import .
docker run --name teedy-import -d -v /path/to/preferencefile:/root/.config/preferences/com.sismics.docs.importer.pref -v /path/to/import/folder:/import teedy-import
```
### Environment variables
Instead of mounting the preferences file, the options can also be set by setting the environment variables `tag`, `addTags`, `lang`, `baseUrl`, `username` and password `password`.
The latter three have to be set for the importer to work. The value of `tag` has to be set to the UUID of the tag, not the name (The UUID can be found by visiting `baseUrl/api/tag/list` in your browser).
Example usage:

```docker
docker build -t teedy-import .
docker run --name teedy-import -d -e tag= -e addTags=false -e lang=eng -e baseUrl='http://teedy.example.com:port' -e username=username -e password=superSecretPassword -v /path/to/import/folder:/import teedy-import
```

## Build from sources

```console
npm install
npm install -g pkg
pkg .
```
