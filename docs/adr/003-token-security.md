# ADR 003 — Sécurité des API Tokens

## Status
Acceptée

## Décision
- Génération : 256 bits aléatoires (`SecureRandom`), encodage Base64URL, préfixe `aip_live_`
- Stockage : **uniquement** SHA-256 hash en DB
- Affichage : plaintext montré **une seule fois** à la création
- Validation : O(1) lookup par hash
- Scopes granulaires (CHAT_READ, PAYROLL_WRITE, etc.) appliqués par chaque service
- Rotation : nouveau secret + invalidation atomique
- IP allowlist : CIDR optionnel par token
- Audit : `usage_count` + `last_used_at` + logs Loki

## Conséquences
+ Aucun risque de fuite via dump DB
+ Conformité ANSSI / OWASP
- Impossible de récupérer un token oublié → rotation obligatoire

