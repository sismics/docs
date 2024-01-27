FROM ubuntu:22.04

# Run Debian in non interactive mode
ENV DEBIAN_FRONTEND noninteractive

# Configure settings
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
RUN ln -fs /usr/share/zoneinfo/Europe/Paris /etc/localtime
RUN apt-get update && apt-get -y -q install --reinstall tzdata
RUN dpkg-reconfigure -f noninteractive tzdata
COPY docker/etc /etc
RUN echo "for f in \`ls /etc/bashrc.d/*\`; do . \$f; done;" >> ~/.bashrc
RUN apt-get -y -q install vim less procps unzip wget && \
    rm -rf /var/lib/apt/lists/*

RUN apt-get update && \
    apt-get -y -q install openjdk-11-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
ENV JAVA_OPTS -Duser.timezone=Europe/Paris -Dfile.encoding=UTF-8 -Xmx1024m

ENV JETTY_VERSION 11.0.14
RUN wget -nv -O /tmp/jetty.tar.gz \
    "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-home/${JETTY_VERSION}/jetty-home-${JETTY_VERSION}.tar.gz" \
    && tar xzf /tmp/jetty.tar.gz -C /opt \
    && mv /opt/jetty* /opt/jetty \
    && useradd jetty -U -s /bin/false \
    && chown -R jetty:jetty /opt/jetty \
    && mkdir /opt/jetty/webapps
WORKDIR /opt/jetty
RUN chmod +x bin/jetty.sh

# Init configuration
COPY docker/opt /opt
EXPOSE 8080
ENV JETTY_HOME /opt/jetty
ENV JAVA_OPTIONS -Xmx512m

LABEL maintainer="b.gamard@sismics.com"

RUN apt-get update && \
    apt-get -y -q --no-install-recommends install \
    ffmpeg \
    mediainfo \
    tesseract-ocr \
    tesseract-ocr-ara \
    tesseract-ocr-ces \
    tesseract-ocr-chi-sim \
    tesseract-ocr-chi-tra \
    tesseract-ocr-dan \
    tesseract-ocr-deu \
    tesseract-ocr-fin \
    tesseract-ocr-fra \
    tesseract-ocr-heb \
    tesseract-ocr-hin \
    tesseract-ocr-hun \
    tesseract-ocr-ita \
    tesseract-ocr-jpn \
    tesseract-ocr-kor \
    tesseract-ocr-lav \
    tesseract-ocr-nld \
    tesseract-ocr-nor \
    tesseract-ocr-pol \
    tesseract-ocr-por \
    tesseract-ocr-rus \
    tesseract-ocr-spa \
    tesseract-ocr-swe \
    tesseract-ocr-tha \
    tesseract-ocr-tur \
    tesseract-ocr-ukr \
    tesseract-ocr-vie \
    tesseract-ocr-sqi && \
    apt-get clean && rm -rf /var/lib/apt/lists/* && \
    mkdir /app && \
    cd /app && \
    java -jar /opt/jetty/start.jar --add-modules=server,http,webapp,deploy

ADD docs.xml /app/webapps/docs.xml
ADD docs-web/target/docs-web-*.war /app/webapps/docs.war

ENV JAVA_OPTIONS -Xmx1g

WORKDIR /app
# Set the default command to run when starting the container
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

CMD ["/entrypoint.sh"]
