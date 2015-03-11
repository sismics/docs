#!/bin/bash

docker rm -f sismics_docs
docker run \
    -d --name=sismics_docs --restart=always \
    --volumes-from=sismics_docs_data \
    -e 'VIRTUAL_HOST_SECURE=docs.sismics.com' -e 'VIRTUAL_PORT=80' \
    sismics/docs:latest
