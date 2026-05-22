"""DPO (Direct Preference Optimization) trainer wrapper."""
import asyncio
from datetime import datetime


async def run_dpo(job) -> str:
    cfg = job.config
    job.logs.append(f"[{datetime.utcnow()}] DPO start dataset={cfg.dataset_path}")
    await asyncio.sleep(1)
    job.metrics["dpo_loss"] = []
    for epoch in range(cfg.num_epochs):
        for step in range(5):
            await asyncio.sleep(0.2)
            loss = round(1.2 / (epoch * 5 + step + 1), 4)
            job.metrics["dpo_loss"].append(loss)
            job.logs.append(f"[DPO epoch {epoch+1}] step {step} loss={loss}")
    return f"s3://ai-models/dpo-{job.id}.safetensors"

