FROM node:22-alpine AS build
WORKDIR /workspace

ARG NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_API_BASE_URL=${NEXT_PUBLIC_API_BASE_URL}

COPY frontend/package.json frontend/package.json
RUN cd frontend && npm install

COPY frontend frontend
RUN cd frontend && npm run build && npm prune --omit=dev

FROM node:22-alpine
WORKDIR /app

ENV NODE_ENV=production
ARG NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_API_BASE_URL=${NEXT_PUBLIC_API_BASE_URL}

COPY --from=build /workspace/frontend/.next ./.next
COPY --from=build /workspace/frontend/public ./public
COPY --from=build /workspace/frontend/package.json ./package.json
COPY --from=build /workspace/frontend/node_modules ./node_modules
COPY --from=build /workspace/frontend/next.config.ts ./next.config.ts

EXPOSE 3000

CMD ["npm", "run", "start"]
