FROM sismics/jetty:9.3.11-jdk7
MAINTAINER benjamin.gam@gmail.com

RUN apt-get -y -q install tesseract-ocr tesseract-ocr-fra tesseract-ocr-jpn

ENV TESSDATA_PREFIX /usr/share/tesseract-ocr
ENV LC_NUMERIC C

ADD docs-web/target/docs-web-*.war /opt/jetty/webapps/docs.war
ADD docs.xml /opt/jetty/webapps/docs.xml
