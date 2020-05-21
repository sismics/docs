FROM node:14.2-alpine AS builder
WORKDIR /build
COPY node_modules/ ./node_modules/
COPY main.js package-lock.json package.json ./
RUN npm install && npm install -g pkg
RUN pkg -t node14-alpine-x64 .

FROM alpine
RUN apk add --no-cache \
    libc6-compat \
    libstdc++ 
COPY --from=builder /build/teedy-importer ./

RUN mkdir -p /root/.config/preferences && touch /root/.config/preferences/com.sismics.docs.importer.pref && mkdir -p /import

CMD ["/teedy-importer","-d"]
