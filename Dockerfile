FROM maven:3-jdk-11 as builder

WORKDIR /tmp/fhir-server
COPY . .
RUN mvn clean install -DskipTests


FROM tomcat:9-jre11

WORKDIR /usr/local/tomcat/webapps
RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles && \
	rm -rf ROOT
COPY --from=builder /tmp/fhir-server/target/fhir-server.war ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
