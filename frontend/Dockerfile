# 使用多阶段构建
# 第一阶段：构建阶段
FROM node:16-alpine AS build-stage
WORKDIR /app

# 复制package文件并安装依赖（利用Docker缓存）
COPY package*.json ./
RUN npm install

# 复制源代码并构建
COPY . .
RUN npm run build-only

# 第二阶段：生产环境阶段
FROM nginx:alpine AS production-stage

# 复制自定义nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建好的文件
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动nginx（nginx镜像默认已有启动命令）