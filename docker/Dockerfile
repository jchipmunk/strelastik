FROM openjdk:8-jre-alpine3.9

ENV STRELASTIK_HOME=/opt/strelastik

RUN set -x \
    && apk --update --no-cache add bash curl rsync vim tini ttyd jattach \
    && rm -rf /var/cache/apk/*

# Add user
RUN addgroup -S strelastik && adduser -S -G strelastik strelastik

ARG DISTRO_NAME=strelastik-0.0.1-SNAPSHOT.tar
ARG TMP_DIR=/tmp

COPY build/distributions/${DISTRO_NAME} ${TMP_DIR}
RUN mkdir -p ${STRELASTIK_HOME} \
    && tar -xf ${TMP_DIR}/${DISTRO_NAME} -C ${STRELASTIK_HOME} --strip-components=1 \
    && rm -rf ${TMP_DIR}/${DISTRO_NAME}*

COPY docker/docker-entrypoint.sh /

RUN set -x \
    && for path in \
         /var/opt/strelastik/profile \
    ; do \
        mkdir -p "$path"; \
        chown -R strelastik:strelastik "$path"; \
    done \
    && chmod +x /docker-entrypoint.sh

USER strelastik
WORKDIR ${STRELASTIK_HOME}

# Expose the port
EXPOSE 8080

ENTRYPOINT ["tini", "--", "/docker-entrypoint.sh"]
CMD ["start"]