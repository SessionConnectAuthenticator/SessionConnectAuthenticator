FROM jboss/keycloak
ENV KEYCLOAK_USER admin
ENV KEYCLOAK_PASSWORD admin
ENV SESSIONCONNECT_REALM master
COPY module/*.jar keycloak/spi-modules/
COPY ftl/*.ftl keycloak/themes/keycloak/login/
RUN keycloak/bin/jboss-cli.sh --command="module add --name=de.tud.sep.mbi.sessionconnect-webservice --resources=keycloak/spi-modules/Webservice.jar --dependencies=javax.ws.rs.api,org.keycloak.keycloak-core,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,org.keycloak.keycloak-model-infinispan,org.infinispan,org.jboss.logging"
RUN keycloak/bin/jboss-cli.sh --command="module add --name=de.tud.sep.mbi.sessionconnect-authenticator --resources=keycloak/spi-modules/Authenticator.jar --dependencies=javax.ws.rs.api,org.keycloak.keycloak-core,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,org.keycloak.keycloak-model-infinispan,org.keycloak.keycloak-common,org.infinispan,org.jboss.logging,org.jboss.resteasy.resteasy-jaxrs"
RUN sed -i 's|classpath:${jboss.home.dir}/providers/\*|classpath:${jboss.home.dir}/providers/\* </provider> <provider> module:de.tud.sep.mbi.sessionconnect-webservice|g' keycloak/standalone/configuration/standalone.xml
RUN sed -i 's|classpath:${jboss.home.dir}/providers/\*|classpath:${jboss.home.dir}/providers/\* </provider> <provider> module:de.tud.sep.mbi.sessionconnect-authenticator|g' keycloak/standalone/configuration/standalone.xml