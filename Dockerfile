FROM sismics/ubuntu-jetty:9.3.11
MAINTAINER b.gamard@sismics.com

RUN apt-get update && apt-get -y -q install ffmpeg mediainfo tesseract-ocr tesseract-ocr-fra tesseract-ocr-ita tesseract-ocr-kor tesseract-ocr-rus tesseract-ocr-ukr tesseract-ocr-spa tesseract-ocr-ara tesseract-ocr-hin tesseract-ocr-deu tesseract-ocr-pol tesseract-ocr-jpn tesseract-ocr-por tesseract-ocr-tha tesseract-ocr-jpn tesseract-ocr-chi-sim tesseract-ocr-chi-tra && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV TESSDATA_PREFIX /usr/share/tesseract-ocr
ENV LC_NUMERIC C

# Remove the embedded javax.mail jar from Jetty
RUN rm -f /opt/jetty/lib/jndi/javax.mail.glassfish-*.jar

ADD docs.xml /opt/jetty/webapps/docs.xml
ADD docs-web/target/docs-web-*.war /opt/jetty/webapps/docs.war
