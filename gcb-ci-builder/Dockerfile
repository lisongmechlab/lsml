FROM ubuntu:latest

ENV LANG=en_US.UTF-8 \ 
    JAVA_PKG=jdk-8u301-linux-x64.tar.gz \
    JAVA_SHA256=f87ea91eb825ee45366f2653d97f8ca96527809a37982bad8fff19a2eaf3cd42 \
    JAVA_VERSION=1.8.0_301 \
    JAVA_HOME=/usr/java/jdk-8 \
    PATH=$JAVA_HOME/bin:$PATH

COPY $JAVA_PKG /tmp/jdk.tgz

RUN set -eux; \
    echo "$JAVA_SHA256 */tmp/jdk.tgz" | sha256sum -c -; \
    mkdir -p "$JAVA_HOME"; \
    tar -zxvf /tmp/jdk.tgz -C "$JAVA_HOME" --strip-components 1; \
    update-alternatives --install "/usr/bin/java" "java" "$JAVA_HOME/bin/java" 1; \
    update-alternatives --set java "$JAVA_HOME/bin/java"; \
    rm /tmp/jdk.tgz

ENTRYPOINT [ "/bin/bash", "-l", "-c" ]
