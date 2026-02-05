#!/bin/bash

# Yanxue Platform 一键启动脚本

set -e

PROJECT_DIR="/poject/bysj/yanxue-platform"
SERVER_LOG="/tmp/yanxue-server.log"
ADMIN_LOG="/tmp/yanxue-admin.log"

echo "======================================"
echo "   Yanxue Platform 启动脚本"
echo "======================================"

# 1. 启动数据库服务
echo ""
echo "[1/3] 启动数据库服务..."
docker start yanxue-mysql yanxue-redis
echo "  - MySQL (端口 13306) 已启动"
echo "  - Redis (端口 16379) 已启动"

# 等待数据库就绪
echo "  - 等待数据库就绪..."
sleep 3

# 2. 启动后端服务
echo ""
echo "[2/3] 启动后端服务..."
cd "$PROJECT_DIR/yanxue-server"
nohup mvn spring-boot:run > "$SERVER_LOG" 2>&1 &
echo "  - 后端服务已启动 (端口 8080)"
echo "  - 日志: $SERVER_LOG"

# 等待后端启动
sleep 5

# 3. 启动前端服务
echo ""
echo "[3/3] 启动前端服务..."
cd "$PROJECT_DIR/yanxue-admin"
nohup npm run dev -- --host 0.0.0.0 > "$ADMIN_LOG" 2>&1 &
echo "  - 前端服务已启动 (端口 5173)"
echo "  - 日志: $ADMIN_LOG"

echo ""
echo "======================================"
echo "   启动完成"
echo "======================================"
echo ""
echo "访问地址:"
echo "  - 前端: http://localhost:5173"
echo "  - 后端: http://localhost:8080"
echo ""
echo "查看日志:"
echo "  - tail -f $SERVER_LOG"
echo "  - tail -f $ADMIN_LOG"
echo ""
