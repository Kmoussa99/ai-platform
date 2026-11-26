# Tag flottant conserve : la version corrigee 21.0.13.1+2 n'est publiee qu'en amd64 pour cette
# variante. Sur amd64, ce tag fournit deja 21.0.13.1+2 (reconstruire avec --pull) ; sur ARM,
# CVE-2026-114938 (Java4Shell) ne s'applique pas.
FROM eclipse-temurin:21-jre
# CVE-2026-114938 (Java4Shell) : desactive l'intrinsic Base64 vulnerable de HotSpot, en defense
# en profondeur du JDK corrige. Flag accepte sur x86-64 comme sur ARM.
ENV JAVA_TOOL_OPTIONS="-XX:-UseBASE64Intrinsics"
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]

