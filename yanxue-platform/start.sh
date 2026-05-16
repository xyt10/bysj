#!/bin/bash

# Yanxue Platform 一键启动脚本

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVER_LOG="/tmp/yanxue-server.log"
SERVER_BUILD_LOG="/tmp/yanxue-server-build.log"
ADMIN_LOG="/tmp/yanxue-admin.log"
USER_LOG="/tmp/yanxue-user.log"
SERVER_PORT="${SERVER_PORT:-18080}"
ADMIN_PORT="${ADMIN_PORT:-13000}"
USER_PORT="${USER_PORT:-13001}"
SERVER_JAR="$PROJECT_DIR/yanxue-server/target/yanxue-server-1.0.0.jar"

wait_for_port() {
  local host="$1"
  local port="$2"
  local timeout="$3"
  local name="$4"
  local elapsed=0

  while (( elapsed < timeout )); do
    if (echo > "/dev/tcp/${host}/${port}") >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
    elapsed=$((elapsed + 1))
  done

  echo "  - ${name} 启动超时（>${timeout}s，端口 ${port} 未就绪）"
  return 1
}

wait_for_http() {
  local url="$1"
  local timeout="$2"
  local name="$3"
  local expect_pattern="${4:-}"
  local elapsed=0

  while (( elapsed < timeout )); do
    local body
    local code
    body="$(curl -sS "$url" 2>/dev/null || true)"
    code="$(curl -sS -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo 000)"

    if [[ "$code" != "000" && "$code" != 5* ]]; then
      if [[ -z "$expect_pattern" || "$body" == *"$expect_pattern"* ]]; then
        return 0
      fi
    fi

    sleep 1
    elapsed=$((elapsed + 1))
  done

  echo "  - ${name} HTTP 检查失败（${url}）"
  return 1
}

start_backend() {
  if pgrep -f "yanxue-server-1.0.0.jar|spring-boot:run.*yanxue-server|YanxueApplication" >/dev/null 2>&1; then
    echo "  - 后端服务已在运行，跳过启动"
  else
    echo "  - 正在构建后端..."
    if ! mvn -q -DskipTests package > "$SERVER_BUILD_LOG" 2>&1; then
      echo "  - 后端构建失败，日志（最后40行）:"
      tail -n 40 "$SERVER_BUILD_LOG" || true
      exit 1
    fi

    nohup java -jar "$SERVER_JAR" > "$SERVER_LOG" 2>&1 &
    echo "  - 后端服务启动命令已提交"
  fi

  if wait_for_port "127.0.0.1" "$SERVER_PORT" 60 "后端服务"     && wait_for_http "http://127.0.0.1:${SERVER_PORT}/" 20 "后端服务"; then
    if pgrep -f "yanxue-server-1.0.0.jar|spring-boot:run.*yanxue-server|YanxueApplication" >/dev/null 2>&1; then
      echo "  - 后端服务已就绪 (端口 ${SERVER_PORT})"
    else
      echo "  - 后端端口短暂可达，但进程已退出"
      tail -n 40 "$SERVER_LOG" || true
      exit 1
    fi
  else
    echo "  - 后端日志（最后20行）:"
    tail -n 20 "$SERVER_LOG" || true
    exit 1
  fi

  echo "  - 日志: $SERVER_LOG"
}

start_admin() {
  if wait_for_http "http://127.0.0.1:${ADMIN_PORT}/" 2 "管理端" "<title>研学旅行平台管理后台</title>" >/dev/null 2>&1; then
    echo "  - 管理端已在运行，跳过启动"
  else
    nohup npm run dev -- --host 0.0.0.0 --strictPort --port ${ADMIN_PORT} > "$ADMIN_LOG" 2>&1 &
    echo "  - 管理端启动命令已提交"
  fi

  if wait_for_port "127.0.0.1" "$ADMIN_PORT" 45 "管理端"     && wait_for_http "http://127.0.0.1:${ADMIN_PORT}/" 20 "管理端" "<title>研学旅行平台管理后台</title>"; then
    echo "  - 管理端已就绪 (端口 ${ADMIN_PORT})"
  else
    echo "  - 管理端日志（最后20行）:"
    tail -n 20 "$ADMIN_LOG" || true
    exit 1
  fi

  echo "  - 日志: $ADMIN_LOG"
}

start_user() {
  if wait_for_http "http://127.0.0.1:${USER_PORT}/" 2 "用户端" "<title>研学旅行平台</title>" >/dev/null 2>&1; then
    echo "  - 用户端已在运行，跳过启动"
  else
    nohup npm run dev -- --host 0.0.0.0 --strictPort --port ${USER_PORT} > "$USER_LOG" 2>&1 &
    echo "  - 用户端启动命令已提交"
  fi

  if wait_for_port "127.0.0.1" "$USER_PORT" 45 "用户端"     && wait_for_http "http://127.0.0.1:${USER_PORT}/" 20 "用户端" "<title>研学旅行平台</title>"; then
    echo "  - 用户端已就绪 (端口 ${USER_PORT})"
  else
    echo "  - 用户端日志（最后20行）:"
    tail -n 20 "$USER_LOG" || true
    exit 1
  fi

  echo "  - 日志: $USER_LOG"
}

echo "======================================"
echo "   Yanxue Platform 启动脚本"
echo "======================================"

# 1. 启动数据库服务
echo ""
echo "[1/4] 启动数据库服务..."
docker start yanxue-mysql yanxue-redis >/dev/null

for container in yanxue-mysql yanxue-redis; do
  status="$(docker inspect -f '{{.State.Status}}' "$container" 2>/dev/null || echo 'unknown')"
  if [[ "$status" != "running" ]]; then
    echo "  - ${container} 未运行，当前状态: ${status}"
    exit 1
  fi
done

echo "  - MySQL (端口 13306) 已启动"
echo "  - Redis (端口 16379) 已启动"

# 等待数据库就绪
echo "  - 等待数据库就绪..."
sleep 3

# 2. 启动后端服务
echo ""
echo "[2/4] 启动后端服务..."
cd "$PROJECT_DIR/yanxue-server"
start_backend

# 3. 启动前端服务
echo ""
echo "[3/4] 启动管理端前端..."
cd "$PROJECT_DIR/yanxue-admin"
start_admin

# 4. 启动用户端服务
echo ""
echo "[4/4] 启动用户端前端..."
cd "$PROJECT_DIR/yanxue-user"
start_user

echo ""
echo "======================================"
echo "   启动完成"
echo "======================================"
echo ""
echo "访问地址:"
echo "  - 管理端: http://localhost:${ADMIN_PORT}"
echo "  - 用户端: http://localhost:${USER_PORT}"
echo "  - 后端: http://localhost:${SERVER_PORT}"
echo ""
echo "查看日志:"
echo "  - tail -f $SERVER_LOG"
echo "  - tail -f $ADMIN_LOG"
echo "  - tail -f $USER_LOG"
echo ""
