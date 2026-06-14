# Multi-stage Dockerfile for thymeleafexamples-gtvg
# Stage 1: build the WAR using Maven
# Use a JDK11-based Maven image for the build (widely available). The project is configured
# to compile to Java 8 bytecode via maven.compiler.release so building with JDK11 is fine.
FROM maven:3.8.8 AS builder
WORKDIR /build
# copy everything except files excluded by .dockerignore so Maven has full project context
COPY . .
RUN mvn -B -DskipTests package

# Stage 2: run on Tomcat 10 using an available JDK11 Temurin tag. This avoids missing-image
# metadata errors for less-common tags like jdk8-corretto on some registries.
FROM tomcat:10
# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*
# Copy WAR from builder stage and deploy as ROOT.war
# Copy WAR from builder stage and deploy as ROOT.war
COPY --from=builder /build/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat port
EXPOSE 8080

# Use the default Tomcat startup script
CMD ["catalina.sh", "run"]
