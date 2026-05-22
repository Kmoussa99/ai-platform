# ADR 002 — RAG hybride avec Qdrant + Ollama

## Status
Acceptée

## Décision
- **Vector store** : Qdrant self-hosted (HNSW + filtrage par payload)
- **Embeddings** : `nomic-embed-text` via Ollama (768 dim, multilingue)
- **LLM** : Ollama (Qwen2.5-Coder, Llama3) en local, fallback Claude API
- **Pipeline** : query expansion → embed → recherche dense → filtrage `data_access_level` → injection contexte avec citations [n]

## Alternatives écartées
- **Pinecone/Weaviate cloud** : viole l'exigence self-hosted
- **PgVector** : suffisant <1M chunks, mais pas de hybrid search natif
- **OpenAI embeddings** : envoi de données hors VPC interdit

