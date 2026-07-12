# ============================================================
# AI Mock Interview Platform — Speech Service
# Python FastAPI + faster-whisper
# Module 4 — Speech Module Integration
# ============================================================

# ---- Build stage ----
FROM python:3.11-slim AS builder

WORKDIR /build

# Install build tools (needed by some wheels)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# ---- Runtime stage ----
FROM python:3.11-slim AS runtime

WORKDIR /app

# ffmpeg required by faster-whisper for audio decoding
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Copy installed packages from builder
COPY --from=builder /install /usr/local

# Copy application source
COPY main.py whisper_engine.py ./

# Environment defaults
ENV WHISPER_MODEL_SIZE=base \
    PORT=8001 \
    RELOAD=false

EXPOSE 8001

# Pre-download the model at build time to avoid cold-start delay in containers.
# Remove this line if you prefer on-demand model download (saves image size).
RUN python -c "from faster_whisper import WhisperModel; WhisperModel('base', device='cpu', compute_type='int8')" \
    || echo "Model pre-download failed — will download on first request"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8001/health')"

CMD ["python", "main.py"]
