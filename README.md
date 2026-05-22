# Enterprise AI Platform — Côte d'Ivoire 🇨🇮

Plateforme IA interne self-hosted, sécurisée, multi-agents, RAG + fine-tuning continu, conforme au droit ivoirien (paie, CNPS, DGI, SYSCOHADA).

## 🚀 Quickstart (5 minutes)

```bash
# 1. Cloner et entrer dans le projet
cd ai-platform

# 2. Démarrer toute la stack
chmod +x scripts/init-multi-db.sh
docker compose up -d

# 3. Attendre que Keycloak démarre (~1min) puis vérifier
open http://localhost:8080   # Keycloak (admin/admin)
open http://localhost:4200   # Frontend Angular
open http://localhost:9001   # MinIO Console (minio_admin/minio_admin_pwd)
open http://localhost:6333/dashboard  # Qdrant
open http://localhost:3000   # Grafana (admin/admin)

# 4. Pull des modèles Ollama (dans un autre terminal)
docker exec -it ai-ollama ollama pull qwen2.5-coder:7b
docker exec -it ai-ollama ollama pull nomic-embed-text
```

## 🧱 Architecture

```
                      ┌──────────────┐
                      │  Frontend    │  Angular 17 (signals + SSR)
                      │  :4200       │
                      └──────┬───────┘
                             │
                      ┌──────▼───────┐    ┌──────────┐
                      │   Gateway    │◄──►│ Keycloak │
                      │   :8000      │    │  :8080   │
                      └──┬─┬─┬─┬─┬─┬─┘    └──────────┘
            ┌────────────┘ │ │ │ │ └──────────────┐
            ▼              ▼ ▼ ▼ ▼                ▼
       ┌─────────┐  ┌──────┐ ... ┌─────────┐  ┌─────────┐
       │  auth   │  │ chat │     │ payroll │  │   ml    │
       │ :8081   │  │:8082 │     │  :8085  │  │ :8086   │
       └─────────┘  └──┬───┘     └────┬────┘  └────┬────┘
                       │              │            │
                  ┌────▼────┐    ┌────▼────┐  ┌───▼───┐
                  │ Qdrant  │    │ Postgres│  │Ollama │
                  │ :6333   │    │  :5432  │  │:11434 │
                  └─────────┘    └─────────┘  └───────┘
```

## 📚 Modules

| Module | Port | Description |
|---|---|---|
| `gateway` | 8000 | Spring Cloud Gateway, JWT, rate limit, DLP |
| `auth-service` | 8081 | RBAC, audit log, profil utilisateur |
| `chat-service` | 8082 | Conversations, RAG, tools, streaming |
| `token-service` | 8083 | API tokens scopés |
| `document-service` | 8084 | Ingestion, chunking, indexation Qdrant |
| `payroll-service` | 8085 | Paie CI (CNPS, DGI, SYSCOHADA) |
| `ml-service` | 8086 | FastAPI, LoRA/QLoRA/DPO/SFT, MLflow |
| `frontend` | 4200 | Angular 17 |

## 👤 Comptes de démo

- `admin.demo / admin123` — ROLE_ADMIN, L3
- `dev.demo / dev123` — ROLE_DEVELOPER, L2
- `compta.demo / compta123` — ROLE_ACCOUNTANT, L2

## 🧪 Tests

```bash
# Backend (chaque service)
cd payroll-service && ./mvnw test

# Frontend
cd frontend && npm test

# Charge (Gatling)
cd gateway && ./mvnw gatling:test
```

## 🔐 Sécurité

- JWT validés à chaque requête au gateway
- DLP middleware : email, téléphone, IBAN, numéro CI, secrets
- Hash SHA-256 des API tokens (jamais stockés en clair)
- Audit log toutes actions sensibles
- Inputs sanitizés (SQL/XSS) via Spring Security + JPA

## 📜 ADRs

Voir `docs/adr/` pour les décisions d'architecture.

