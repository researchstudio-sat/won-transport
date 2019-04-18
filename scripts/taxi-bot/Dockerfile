# before docker build can be executed, the jar file and the conf
# directory have to be copied into this folder (done by maven build)

# use java as a base image
# fix java version here until the following issue is resolved: https://github.com/researchstudio-sat/webofneeds/issues/1229
FROM openjdk:8u121-jdk

# add webofneeds default config env variables
ENV WON_CONFIG_DIR=/usr/src/conf
ENV LOGBACK_CONFIG=logback.xml

# add the default monitoring output directory
ENV TAXI_BOT=won.transport.taxi.bot.app.TaxiBotApp

# add the jar and the conf directory
ADD ./taxi-bot.jar /usr/src/
ADD ./conf ${WON_CONFIG_DIR}
ADD ./factory-atoms /usr/src/factory-atoms

# add certificates directory
RUN mkdir -p /usr/src/certs
RUN mkdir -p /usr/src/factory-atoms


# start echo bot
WORKDIR /usr/src/
CMD java -DWON_CONFIG_DIR=${WON_CONFIG_DIR}/ \
-Dlogback.configurationFile=${WON_CONFIG_DIR}/${LOGBACK_CONFIG} \
${JMEM_OPTS} \
${JMX_OPTS} \
-cp "taxi-bot.jar" ${TAXI_BOT}