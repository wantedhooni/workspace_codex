docker compose \
-f docker-compose-infra.yml \
-f docker-compose-app.yml down -v

docker compose \
-f docker-compose-infra.yml \
-f docker-compose-app.yml up --build
