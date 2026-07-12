package com.interviewplatform.speech.controller;

import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.speech.dto.TranscriptionResult;
import com.interviewplatform.speech.service.SpeechService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/speech")
@RequiredArgsConstructor
@Tag(name = "Speech", description = "Speech-to-Text Transcription API")
public class SpeechController {

    private final SpeechService speechService;

    @PostMapping(value = "/transcribe", consumes = "multipart/form-data")
    @Operation(summary = "Transcribe audio to text", description = "Proxies the audio to the STT service and returns the transcript.")
    public ApiResponse<TranscriptionResult> transcribe(
            @RequestPart("audio") MultipartFile audio) throws IOException {
        
        log.info("Received direct transcribe request for audio of size: {}", audio.getSize());
        
        // Generate a temporary UUID since this is a direct proxy call
        UUID tempAnswerId = UUID.randomUUID();
        
        // Default format to webm for now, ideally extracted from content type
        String format = "webm"; 
        
        TranscriptionResult result = speechService.transcribe(tempAnswerId, audio.getBytes(), format);
        return ApiResponse.success(result);
    }
}
