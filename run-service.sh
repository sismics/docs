#!/bin/bash
docker rm -f sismics_docs
docker run \
    -d --name=sismics_docs --restart=always \
    -v sismics_docs_data:/data \
    -e 'VIRTUAL_HOST_SECURE=docs.bgamard.org' -e 'VIRTUAL_PORT=80' \
    sismics/docs:latest
