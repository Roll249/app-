#!/bin/bash
# =============================================================================
# Personal Finance App - Docker 便捷启动脚本
# 用于快速启动/停止/管理 Docker 容器
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# -----------------------------------------------------------------------------
# 辅助函数
# -----------------------------------------------------------------------------

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  Personal Finance App - Docker${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# -----------------------------------------------------------------------------
# 主命令
# -----------------------------------------------------------------------------

show_help() {
    echo "用法: $0 <command> [options]"
    echo ""
    echo "命令:"
    echo "  start         启动所有服务"
    echo "  stop          停止所有服务"
    echo "  restart       重启所有服务"
    echo "  logs [svc]    查看日志 (可选: postgres, backend, pgadmin)"
    echo "  status        查看服务状态"
    echo "  clean         清理所有容器和数据卷"
    echo "  rebuild       重新构建并启动"
    echo "  dev           启动开发模式 (包含 pgAdmin)"
    echo "  shell [svc]   进入容器 shell (postgres, backend)"
    echo "  db            打开 PostgreSQL psql 客户端"
    echo "  help          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start              # 启动服务"
    echo "  $0 logs backend       # 查看后端日志"
    echo "  $0 dev                # 启动开发模式"
    echo "  $0 clean              # 清理所有数据"
}

# 检查 Docker 是否运行
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker 未运行，请先启动 Docker"
        exit 1
    fi
}

# 复制环境变量文件
setup_env() {
    if [ ! -f "$PROJECT_ROOT/.env" ]; then
        if [ -f "$PROJECT_ROOT/.env.example" ]; then
            print_warning "未找到 .env 文件，正在从 .env.example 创建..."
            cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
            print_success "已创建 .env 文件，请根据需要修改配置"
        else
            print_error "未找到 .env.example 文件"
            exit 1
        fi
    fi
}

# -----------------------------------------------------------------------------
# 命令实现
# -----------------------------------------------------------------------------

cmd_start() {
    check_docker
    setup_env
    cd "$PROJECT_ROOT"
    docker compose up -d
    print_success "服务已启动!"
    echo ""
    echo "访问地址:"
    echo "  - 后端 API:  http://localhost:3000"
    echo "  - 健康检查:  http://localhost:3000/health"
    echo ""
    echo "运行 '$0 status' 查看详细状态"
}

cmd_stop() {
    check_docker
    cd "$PROJECT_ROOT"
    docker compose stop
    print_success "服务已停止"
}

cmd_restart() {
    check_docker
    cd "$PROJECT_ROOT"
    docker compose restart
    print_success "服务已重启"
}

cmd_logs() {
    check_docker
    cd "$PROJECT_ROOT"
    if [ -z "$1" ]; then
        docker compose logs -f
    else
        docker compose logs -f "$1"
    fi
}

cmd_status() {
    check_docker
    cd "$PROJECT_ROOT"
    echo ""
    docker compose ps
    echo ""
    echo "端口检查:"
    if docker compose ps | grep -q "Up"; then
        echo "  - PostgreSQL (5432): $(nc -z localhost 5432 && echo '✓ 运行中' || echo '✗ 未运行')"
        echo "  - Backend (3000):    $(nc -z localhost 3000 && echo '✓ 运行中' || echo '✗ 未运行')"
    fi
}

cmd_clean() {
    check_docker
    echo -e "${RED}警告: 此操作将删除所有数据卷!${NC}"
    read -p "确定要继续吗? (yes/no): " confirm
    if [ "$confirm" = "yes" ]; then
        cd "$PROJECT_ROOT"
        docker compose down -v
        print_success "已清理所有容器和数据卷"
    else
        print_warning "已取消"
    fi
}

cmd_rebuild() {
    check_docker
    setup_env
    cd "$PROJECT_ROOT"
    docker compose down
    docker compose build --no-cache
    docker compose up -d
    print_success "构建并启动完成!"
}

cmd_dev() {
    check_docker
    setup_env
    cd "$PROJECT_ROOT"
    docker compose --profile devtools up -d
    print_success "开发模式已启动!"
    echo ""
    echo "访问地址:"
    echo "  - 后端 API:    http://localhost:3000"
    echo "  - pgAdmin:     http://localhost:5050"
    echo "    邮箱: admin@fintech.local"
    echo "    密码: admin123"
}

cmd_shell() {
    check_docker
    cd "$PROJECT_ROOT"
    case "$1" in
        postgres)
            docker compose exec postgres psql -U postgres -d fintech_db
            ;;
        backend)
            docker compose exec backend sh
            ;;
        *)
            echo "可进入的容器: postgres, backend"
            ;;
    esac
}

cmd_db() {
    check_docker
    cd "$PROJECT_ROOT"
    docker compose exec postgres psql -U postgres -d fintech_db
}

# -----------------------------------------------------------------------------
# 主程序入口
# -----------------------------------------------------------------------------

print_header

case "${1:-help}" in
    start)
        cmd_start
        ;;
    stop)
        cmd_stop
        ;;
    restart)
        cmd_restart
        ;;
    logs)
        cmd_logs "$2"
        ;;
    status)
        cmd_status
        ;;
    clean)
        cmd_clean
        ;;
    rebuild)
        cmd_rebuild
        ;;
    dev)
        cmd_dev
        ;;
    shell)
        cmd_shell "$2"
        ;;
    db)
        cmd_db
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "未知命令: $1"
        show_help
        exit 1
        ;;
esac
