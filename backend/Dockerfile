FROM node:20-slim
WORKDIR /app
COPY package*.json ./
RUN npm ci --production
COPY . .
RUN mkdir -p audio
EXPOSE 3000
CMD ["node", "index.js"]
