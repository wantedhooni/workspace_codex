# Infra

Local development infrastructure.

Services:
- PostgreSQL
- Redis

Run from repo root:

```bash
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml stop
```

Recommended wrapper scripts:

```bash
./scripts/infra-start.sh
./scripts/infra-stop.sh
```
