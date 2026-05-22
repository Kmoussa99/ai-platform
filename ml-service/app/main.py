"""ML Service — fine-tuning + déploiement Ollama."""
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.responses import StreamingResponse, PlainTextResponse
from prometheus_client import Counter, generate_latest, CONTENT_TYPE_LATEST
from pydantic import BaseModel
from enum import Enum
from typing import List, Optional
from uuid import uuid4, UUID
from datetime import datetime
import asyncio
import json
import logging

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("ml-service")

app = FastAPI(title="AI Platform — ML Service",
              description="Fine-tuning LoRA/QLoRA/DPO/SFT, MLflow, déploiement Ollama",
              version="1.0.0")

JOB_REQUESTS = Counter("ml_jobs_total", "Total jobs created")
JOB_FAILED = Counter("ml_jobs_failed_total", "Total failed jobs")


class TrainingMethod(str, Enum):
    LORA = "LORA"
    QLORA = "QLORA"
    DPO = "DPO"
    SFT = "SFT"


class TrainingJobConfig(BaseModel):
    base_model: str = "qwen2.5-coder:7b"
    method: TrainingMethod = TrainingMethod.LORA
    dataset_path: str  # MinIO path to JSONL
    lora_r: int = 16
    lora_alpha: int = 32
    lora_dropout: float = 0.05
    target_modules: List[str] = ["q_proj", "v_proj"]
    num_epochs: int = 3
    batch_size: int = 4
    gradient_accumulation: int = 8
    learning_rate: float = 2e-4
    max_seq_length: int = 4096
    bf16: bool = True
    use_flash_attention: bool = True


class JobStatus(str, Enum):
    QUEUED = "QUEUED"
    RUNNING = "RUNNING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


class Job(BaseModel):
    id: UUID
    config: TrainingJobConfig
    status: JobStatus = JobStatus.QUEUED
    created_at: datetime
    metrics: dict = {}
    logs: List[str] = []
    model_artifact: Optional[str] = None


JOBS: dict[UUID, Job] = {}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/metrics")
def metrics():
    return PlainTextResponse(generate_latest(), media_type=CONTENT_TYPE_LATEST)


@app.post("/ml/jobs/create", response_model=Job)
async def create_job(cfg: TrainingJobConfig, bg: BackgroundTasks):
    JOB_REQUESTS.inc()
    job = Job(id=uuid4(), config=cfg, created_at=datetime.utcnow())
    JOBS[job.id] = job
    bg.add_task(_run_job, job.id)
    return job


async def _run_job(job_id: UUID):
    from training.lora_trainer import run_lora
    from training.dpo_trainer import run_dpo
    job = JOBS[job_id]
    try:
        job.status = JobStatus.RUNNING
        job.logs.append(f"[{datetime.utcnow()}] starting {job.config.method}")
        if job.config.method in (TrainingMethod.LORA, TrainingMethod.QLORA, TrainingMethod.SFT):
            artifact = await run_lora(job)
        else:
            artifact = await run_dpo(job)
        job.model_artifact = artifact
        job.status = JobStatus.COMPLETED
        job.logs.append(f"[{datetime.utcnow()}] completed -> {artifact}")
    except Exception as e:
        JOB_FAILED.inc()
        job.status = JobStatus.FAILED
        job.logs.append(f"[ERROR] {e}")
        log.exception("job failed")


@app.get("/ml/jobs/{job_id}/status", response_model=Job)
def status(job_id: UUID):
    job = JOBS.get(job_id)
    if not job:
        raise HTTPException(404, "job not found")
    return job


@app.get("/ml/jobs/{job_id}/logs")
async def logs(job_id: UUID):
    """SSE streaming logs."""
    async def gen():
        seen = 0
        for _ in range(120):
            job = JOBS.get(job_id)
            if not job:
                break
            while seen < len(job.logs):
                yield f"data: {json.dumps(job.logs[seen])}\n\n"
                seen += 1
            if job.status in (JobStatus.COMPLETED, JobStatus.FAILED, JobStatus.CANCELLED):
                yield "event: done\ndata: end\n\n"
                return
            await asyncio.sleep(1)
    return StreamingResponse(gen(), media_type="text/event-stream")


@app.post("/ml/jobs/{job_id}/cancel", response_model=Job)
def cancel(job_id: UUID):
    job = JOBS.get(job_id)
    if not job:
        raise HTTPException(404)
    job.status = JobStatus.CANCELLED
    return job


@app.get("/ml/models")
def models():
    return [
        {"id": str(j.id), "name": j.config.base_model, "method": j.config.method,
         "artifact": j.model_artifact, "status": j.status}
        for j in JOBS.values() if j.model_artifact
    ]


class DeployRequest(BaseModel):
    target: str = "ollama"


@app.post("/ml/models/{job_id}/deploy")
async def deploy(job_id: UUID, req: DeployRequest):
    job = JOBS.get(job_id)
    if not job or not job.model_artifact:
        raise HTTPException(400, "model not ready")
    # 1) convert to GGUF (call llama.cpp convert script in real impl)
    # 2) push to ollama via REST
    # Stubbed to return success path
    log.info("deploying job=%s artifact=%s target=%s", job_id, job.model_artifact, req.target)
    return {"status": "deployed", "model": f"custom-{job.config.base_model}-{str(job_id)[:8]}"}

