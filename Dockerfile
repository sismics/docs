FROM sismics/ubuntu-jetty:9.4.12-2
MAINTAINER b.gamard@sismics.com

RUN apt-get update && apt-get -y -q install ffmpeg mediainfo tesseract-ocr tesseract-ocr-fra tesseract-ocr-ita tesseract-ocr-kor tesseract-ocr-rus tesseract-ocr-ukr tesseract-ocr-spa tesseract-ocr-ara tesseract-ocr-hin tesseract-ocr-deu tesseract-ocr-pol tesseract-ocr-jpn tesseract-ocr-por tesseract-ocr-tha tesseract-ocr-jpn tesseract-ocr-chi-sim tesseract-ocr-chi-tra tesseract-ocr-nld tesseract-ocr-tur tesseract-ocr-heb tesseract-ocr-hun tesseract-ocr-fin tesseract-ocr-swe tesseract-ocr-lav tesseract-ocr-dan tesseract-ocr-nor && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Remove the embedded javax.mail jar from Jetty
RUN rm -f /opt/jetty/lib/mail/javax.mail.glassfish-*.jar

ADD docs.xml /opt/jetty/webapps/docs.xml
ADD docs-web/target/docs-web-*.war /opt/jetty/webapps/docs.war

ENV JAVA_OPTIONS -Xmx1g