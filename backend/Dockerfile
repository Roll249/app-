# =============================================================================
# Personal Finance App - Backend Dockerfile
# 后端服务 Docker 镜像构建配置
# =============================================================================

# 阶段一: 构建阶段 (builder)
# -----------------------------------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# 复制 package 文件
COPY package*.json ./

# 安装所有依赖（包括 devDependencies 用于构建 TypeScript）
RUN npm ci

# 复制配置文件和源代码
COPY tsconfig.json ./
COPY src ./src

# 编译 TypeScript
RUN npm run build

# 阶段二: 生产运行阶段
# -----------------------------------------------------------------------------
FROM node:20-alpine AS runner

# 安装 dumb-init 用于正确处理信号
RUN apk add --no-cache dumb-init

# 创建非 root 用户
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001

WORKDIR /app

# 从 builder 阶段复制构建产物
COPY --from=builder --chown=nodejs:nodejs /app/node_modules ./node_modules
COPY --from=builder --chown=nodejs:nodejs /app/dist ./dist
COPY --from=builder --chown=nodejs:nodejs /app/package*.json ./

# 设置环境变量
ENV NODE_ENV=production
ENV PORT=3000

# 切换到非 root 用户
USER nodejs

# 暴露端口
EXPOSE 3000

# 使用 dumb-init 作为 PID 1，避免信号处理问题
ENTRYPOINT ["dumb-init", "--"]

# 启动后端服务
CMD ["node", "dist/index.js"]