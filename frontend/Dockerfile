FROM node:20-alpine
LABEL org.opencontainers.image.source=https://github.com/Cloud-Native-Engeneering/Cloud-Native-Engineering-project-group2
WORKDIR /app
ARG NEXT_PUBLIC_BACKEND_URL
ENV NEXT_PUBLIC_BACKEND_URL=$NEXT_PUBLIC_BACKEND_URL
COPY . .
RUN npm ci && npm run build
EXPOSE 3000
CMD ["npm", "start"]
