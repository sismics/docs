FROM jetty:jre8-alpine

USER root

RUN apk add \
    ffmpeg mediainfo tesseract-ocr tesseract-ocr-data-fra tesseract-ocr-data-ita \
    tesseract-ocr-data-kor tesseract-ocr-data-rus tesseract-ocr-data-ukr \
    tesseract-ocr-data-spa tesseract-ocr-data-ara tesseract-ocr-data-hin \
    tesseract-ocr-data-deu tesseract-ocr-data-pol tesseract-ocr-data-jpn \
    tesseract-ocr-data-por tesseract-ocr-data-tha tesseract-ocr-data-jpn \
    tesseract-ocr-data-chi_sim tesseract-ocr-data-chi_tra tesseract-ocr-data-nld \
    tesseract-ocr-data-tur tesseract-ocr-data-heb

USER jetty

COPY ./docs.xml /var/lib/jetty/webapps/
COPY ./docs-web/target/docs-web-*.war /var/lib/jetty/webapps/docs.war