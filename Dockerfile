# 构建阶段 - 使用Maven 3.6.0和JDK 8
FROM maven:3.8.5-openjdk-17 as build

# 指定构建过程中的工作目录
WORKDIR /app

# 复制pom.xml和settings.xml文件
COPY pom.xml settings.xml /app/

# 复制源代码
COPY src /app/src

# 执行代码编译命令（使用国内镜像加速）
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package -DskipTests

# 运行阶段 - JRE17
FROM openjdk:17-jre-alpine

# 设置时区为上海
RUN apk add tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 安装CA证书（用于HTTPS）
RUN apk add --no-cache ca-certificates

# 指定运行时的工作目录
WORKDIR /app

# 从构建阶段复制jar包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口（微信云托管要求使用80端口）
EXPOSE 80

# 设置JVM参数
ENV JAVA_OPTS="-Xmx256m -Xms128m -Djava.security.egd=file:/dev/./urandom"

# 启动应用
CMD java $JAVA_OPTS -jar app.jar --server.port=80