"""
AI Mock Interview Platform — Speech Service
FastAPI application that exposes a /transcribe endpoint backed by faster-whisper.

Module 4 — Speech Module Integration
"""

from __future__ import annotations

import logging
import os
import tempfile
import time
from contextlib import asynccontextmanager
from typing import Optional

import uvicorn
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.responses import JSONResponse

from whisper_engine import WhisperEngine

# ---------------------------------------------------------------------------
# Logging
# ---------------------------------------------------------------------------

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s — %(message)s",
)
logger = logging.getLogger("speech-service")

# ---------------------------------------------------------------------------
# Application lifespan — load model once at startup
# ---------------------------------------------------------------------------

engine: Optional[WhisperEngine] = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load the Whisper model on startup and release on shutdown."""
    global engine
    model_size = os.getenv("WHISPER_MODEL_SIZE", "base")
    logger.info("Loading Whisper model: size=%s", model_size)
    engine = WhisperEngine(model_size=model_size)
    logger.info("Whisper model loaded successfully")
    yield
    logger.info("Speech service shutting down")
    engine = None


# ---------------------------------------------------------------------------
# FastAPI app
# ---------------------------------------------------------------------------

app = FastAPI(
    title="AI Mock Interview — Speech Service",
    description="OpenAI Whisper (faster-whisper) based speech-to-text transcription service.",
    version="1.0.0",
    lifespan=lifespan,
)


# ---------------------------------------------------------------------------
# Health check
# ---------------------------------------------------------------------------

@app.get("/health", summary="Health check")
async def health() -> dict:
    return {
        "status": "UP",
        "model_loaded": engine is not None,
        "model_size": engine.model_size if engine else None,
    }


# ---------------------------------------------------------------------------
# Transcription endpoint
# ---------------------------------------------------------------------------

@app.post("/transcribe", summary="Transcribe audio to text")
async def transcribe(
    audio: UploadFile = File(..., description="Audio file to transcribe"),
    format: str = Form(default="webm", description="Audio format hint (webm, wav, mp3, etc.)"),
    answer_id: Optional[str] = Form(default=None, description="Answer record ID for tracing"),
) -> JSONResponse:
    """
    Accepts a multipart audio file and returns the transcript.

    - **audio**: raw audio bytes (any format supported by ffmpeg)
    - **format**: hint about the audio container (used for the temp file extension)
    - **answer_id**: optional trace ID echoed back in the response

    Returns:
    ```json
    {
      "answer_id": "...",
      "transcript": "Hello, this is my answer.",
      "confidence": 0.92,
      "duration_seconds": 14.5,
      "processing_time_ms": 1230
    }
    ```
    """
    if engine is None:
        raise HTTPException(status_code=503, detail="Whisper model is not loaded")

    start = time.monotonic()

    # Write audio bytes to a temp file so faster-whisper can open it
    suffix = f".{format.lstrip('.')}"
    try:
        with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
            content = await audio.read()
            if not content:
                raise HTTPException(status_code=400, detail="Received empty audio file")
            tmp.write(content)
            tmp_path = tmp.name

        logger.info(
            "Transcribing: answer_id=%s format=%s bytes=%d",
            answer_id, format, len(content),
        )

        result = engine.transcribe(tmp_path)

    except HTTPException:
        raise
    except Exception as exc:
        logger.exception("Transcription error: %s", exc)
        raise HTTPException(status_code=500, detail=f"Transcription failed: {exc}") from exc
    finally:
        # Clean up temp file
        if "tmp_path" in locals():
            try:
                os.unlink(tmp_path)
            except OSError:
                pass

    elapsed_ms = int((time.monotonic() - start) * 1000)
    logger.info(
        "Transcription complete: answer_id=%s duration=%.1fs confidence=%.2f elapsed=%dms",
        answer_id,
        result.get("duration_seconds", 0),
        result.get("confidence", 0),
        elapsed_ms,
    )

    return JSONResponse(
        content={
            "answer_id": answer_id,
            "transcript": result["transcript"],
            "confidence": result["confidence"],
            "duration_seconds": result["duration_seconds"],
            "processing_time_ms": elapsed_ms,
        }
    )


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8001")),
        reload=os.getenv("RELOAD", "false").lower() == "true",
        log_level="info",
    )
