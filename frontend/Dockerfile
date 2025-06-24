# Build stage
FROM node:18-alpine as build

WORKDIR /app

# Package files 복사
COPY package*.json ./

# 의존성 설치 (package-lock.json이 없는 경우 대비)
RUN npm install

# 소스 코드 복사
COPY . .

# 빌드
RUN npm run build

# Production stage
FROM nginx:alpine

# 빌드된 파일 복사
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"] 