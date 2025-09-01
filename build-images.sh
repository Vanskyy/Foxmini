#!/bin/bash

# 设置错误处理：任何命令失败则退出脚本
set -e

echo "开始构建 Foxmini 应用镜像..."

# 只构建镜像，跳过Maven和NPM构建（因为您已经单独构建成功了）
echo "跳过Maven和NPM构建，直接构建Docker镜像..."

# 删除现有镜像（如果存在）
echo "清理旧镜像..."
docker rmi -f fengjy209/foxmini-backend:latest 2>/dev/null || true
docker rmi -f fengjy209/foxmini-frontend:latest 2>/dev/null || true
docker rmi -f fengjy209/foxmini-database:latest 2>/dev/null || true

# 构建镜像
echo "构建 Docker 镜像..."
docker build -t fengjy209/foxmini-backend:latest -f backend/Dockerfile backend
docker build -t fengjy209/foxmini-frontend:latest -f frontend/Dockerfile frontend

# 检查是否存在数据库 Dockerfile，如果存在则构建
if [ -f "database/Dockerfile" ]; then
    docker build -t fengjy209/foxmini-database:latest -f database/Dockerfile database
else
    echo "提示: 未找到数据库 Dockerfile，跳过数据库镜像构建"
fi

echo "镜像构建完成!"