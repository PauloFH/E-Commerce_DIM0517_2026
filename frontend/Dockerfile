FROM oven/bun:1.4.5 AS build
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile --ignore-scripts
COPY . .
RUN bun run build

FROM node:24-alpine
WORKDIR /app
COPY --from=build /app/.output .output
USER node
ENV NODE_ENV=production
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
