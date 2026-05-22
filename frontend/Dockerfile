FROM node:20-alpine AS build
WORKDIR /app

# Cache des dépendances
COPY package.json package-lock.json* ./
RUN npm install --no-audit --no-fund

# Sources et build production
COPY . .
RUN npx ng build --configuration=production

FROM nginx:1.27-alpine
COPY --from=build /app/dist/ai-platform-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/ >/dev/null || exit 1
