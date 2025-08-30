# 使用多阶段构建以减少最终镜像大小
# 第一阶段：构建阶段
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用程序（跳过测试）
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 创建非root用户运行应用
RUN addgroup -S springboot && adduser -S springboot -G springboot
USER springboot

# 从构建阶段复制jar包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口（与application.properties中配置的端口一致）
EXPOSE 8080

# 设置JVM参数（可根据需要调整）
ENV JAVA_OPTS="-XX:MaxRAMPercentage"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]