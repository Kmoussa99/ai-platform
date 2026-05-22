"""Pipeline RAG côté Python (alternative au LangChain4j Java pour des tests/POC)."""
import httpx
from typing import List
from pydantic import BaseModel


class Source(BaseModel):
    chunk_id: str
    title: str
    score: float
    text: str


class RagPipeline:
    def __init__(self, ollama_url: str, qdrant_url: str, embed_model: str = "nomic-embed-text"):
        self.ollama_url = ollama_url
        self.qdrant_url = qdrant_url
        self.embed_model = embed_model

    async def embed(self, text: str) -> List[float]:
        async with httpx.AsyncClient() as c:
            r = await c.post(f"{self.ollama_url}/api/embeddings",
                              json={"model": self.embed_model, "prompt": text})
            return r.json()["embedding"]

    async def search(self, query: str, collection: str = "knowledge_internal",
                     top_k: int = 5) -> List[Source]:
        emb = await self.embed(query)
        async with httpx.AsyncClient() as c:
            r = await c.post(f"{self.qdrant_url}/collections/{collection}/points/search",
                              json={"vector": emb, "limit": top_k, "with_payload": True})
            return [Source(chunk_id=str(p["id"]),
                            title=p["payload"].get("title", ""),
                            score=p["score"],
                            text=p["payload"].get("text", ""))
                    for p in r.json().get("result", [])]

