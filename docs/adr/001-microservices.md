# ADR 001 — Microservices vs Monolithe

## Status
Acceptée

## Contexte
Plateforme IA multi-domaine (chat, paie, ML, documents) avec exigences hétérogènes :
- Paie : transactionnel ACID, faible débit
- Chat : streaming long, scalabilité horizontale
- ML : Python + GPU, cycle de vie indépendant
- Documents : I/O lourd, batch async

## Décision
Architecture **microservices** orchestrée derrière un Spring Cloud Gateway, avec
isolation de bases (1 DB Postgres par service), bus Kafka pour les événements
cross-service (indexation, audit, training feedback).

## Conséquences
+ Scalabilité différenciée (HPA Kubernetes par service)
+ Isolation des pannes
+ Choix techno par service (Java/Python)
- Complexité opérationnelle (gateway, observabilité indispensable)

