FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S springboot && adduser -S springboot -G springboot
USER springboot
COPY target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Duser.timezone=Asia/Shanghai"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]