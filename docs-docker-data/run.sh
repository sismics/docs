#!/bin/sh
docker rm -f sismics_docs_data
docker run --name sismics_docs_data sismics/docs_data
