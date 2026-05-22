# Manifests Kubernetes (production)

Squelettes de déploiement par service. À adapter avec votre registry et votre Vault.

```bash
kubectl apply -f k8s/
```

## Composants suggérés

- **Ingress NGINX** + cert-manager (TLS Let's Encrypt)
- **External Secrets Operator** → HashiCorp Vault
- **Prometheus Operator** + Grafana (kube-prometheus-stack)
- **Loki** + Promtail pour logs
- **PostgreSQL Operator** (Zalando ou CloudNativePG)
- **Qdrant** en StatefulSet 3 replicas
- **Ollama** sur node-pool GPU (taint `accelerator=nvidia`)

## Sécurité production

- NetworkPolicy stricte (deny-all + allow par label)
- PodSecurityStandards: restricted
- Secrets via Vault Agent Injector
- TLS mTLS entre services (Linkerd ou Istio)

