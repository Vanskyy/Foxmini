FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 从构建阶段复制 jar 包
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]