from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json() == {"status": "ok"}

def test_create_job():
    r = client.post("/ml/jobs/create", json={
        "base_model": "qwen2.5-coder:7b",
        "method": "LORA",
        "dataset_path": "s3://ai-datasets/x.jsonl"
    })
    assert r.status_code == 200
    assert r.json()["status"] in ("QUEUED", "RUNNING")

