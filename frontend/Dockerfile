# 使用多阶段构建
FROM node:16-alpine AS build-stage
WORKDIR /app

# 使用正确的 ENV 格式
ENV PATH=/app/node_modules/.bin:$PATH
ENV NODE_OPTIONS=--max_old_space_size=4096

# 复制 package 文件并安装依赖
COPY package*.json ./
RUN npm install

# 复制源代码
COPY . .

# 尝试构建
RUN npm run build-only

# 第二阶段：生产环境阶段
FROM nginx:alpine AS production-stage

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建好的文件
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]