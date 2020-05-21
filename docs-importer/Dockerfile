FROM node:14.2-alpine AS builder
WORKDIR /build
COPY node_modules/ ./node_modules/
COPY main.js package-lock.json package.json ./
RUN npm install && npm install -g pkg
RUN pkg -t node14-alpine-x64 .

FROM alpine
ENV tag= addTags=false lang=eng baseUrl='http://localhost:8080' username=username password=password
RUN apk add --no-cache \
    libc6-compat \
    libstdc++ 
ADD pref /root/.config/preferences/com.sismics.docs.importer.pref
ADD env.sh /
COPY --from=builder /build/teedy-importer ./

CMD ["/bin/ash","-c","/env.sh && /teedy-importer -d"]
