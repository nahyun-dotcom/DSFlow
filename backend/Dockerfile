# Multi-platform compatible base image
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Maven Wrapper 설정 재생성
# 먼저 pom.xml만 복사
COPY pom.xml .

# Maven Wrapper 재생성 (시스템 Maven 사용)
RUN apt-get update && apt-get install -y maven && \
    mvn wrapper:wrapper && \
    chmod +x mvnw

# 의존성 다운로드 (캐시 효율성을 위해)
RUN ./mvnw dependency:go-offline

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN ./mvnw clean package -DskipTests

# 포트 노출
EXPOSE 8080

# JAR 파일명 자동 감지하여 실행
CMD ["sh", "-c", "java -jar target/*.jar"] 