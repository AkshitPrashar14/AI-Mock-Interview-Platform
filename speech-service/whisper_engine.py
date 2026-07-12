"""
Whisper engine wrapper using faster-whisper for CPU/GPU transcription.

Module 4 — Speech Module Integration
"""

from __future__ import annotations

import logging
from typing import Any

logger = logging.getLogger("speech-service.whisper")


class WhisperEngine:
    """
    Thin wrapper around faster-whisper's WhisperModel.

    Supported model sizes (in order of speed/accuracy tradeoff):
      tiny   — ~39M params — fastest, lowest accuracy
      base   — ~74M params — good balance for English (default)
      small  — ~244M params — best English accuracy for interview answers
      medium — ~769M params — high accuracy, slower
      large  — ~1.5B params — highest accuracy, requires GPU

    The model size is controlled via the WHISPER_MODEL_SIZE environment variable.
    """

    def __init__(self, model_size: str = "base") -> None:
        from faster_whisper import WhisperModel  # imported lazily for test isolation

        self.model_size = model_size
        device = "cpu"  # default; set to "cuda" if GPU is available
        compute_type = "int8"  # efficient for CPU; use "float16" on GPU

        logger.info(
            "WhisperEngine: loading model=%s device=%s compute_type=%s",
            model_size, device, compute_type,
        )
        self._model = WhisperModel(model_size, device=device, compute_type=compute_type)
        logger.info("WhisperEngine: model loaded")

    def transcribe(self, audio_path: str) -> dict[str, Any]:
        """
        Transcribes the audio file at the given path.

        Args:
            audio_path: Absolute path to the audio file.

        Returns:
            A dict with keys:
              - transcript (str): concatenated text of all segments
              - confidence (float): average log-probability converted to 0–1 range
              - duration_seconds (float): total audio duration
        """
        logger.debug("WhisperEngine.transcribe: path=%s", audio_path)

        segments, info = self._model.transcribe(
            audio_path,
            beam_size=5,
            language="en",           # force English — can be made configurable later
            vad_filter=True,         # remove silence with voice activity detection
            vad_parameters={
                "min_silence_duration_ms": 500,
            },
        )

        # Collect all segments
        all_segments = list(segments)

        transcript = " ".join(s.text.strip() for s in all_segments if s.text.strip())

        # Compute average confidence from avg_logprob
        confidences = [s.avg_logprob for s in all_segments if s.avg_logprob is not None]
        if confidences:
            import math
            # Convert average log probability to 0–1 probability estimate
            avg_logprob = sum(confidences) / len(confidences)
            confidence = min(1.0, max(0.0, math.exp(avg_logprob)))
        else:
            confidence = 0.0

        duration = info.duration if hasattr(info, "duration") else 0.0

        logger.info(
            "WhisperEngine: transcript=%d chars confidence=%.3f duration=%.1fs",
            len(transcript), confidence, duration,
        )

        return {
            "transcript": transcript,
            "confidence": round(confidence, 4),
            "duration_seconds": round(duration, 2),
        }
