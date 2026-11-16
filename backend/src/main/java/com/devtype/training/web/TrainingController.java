package com.devtype.training.web;

import com.devtype.training.application.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class TrainingController {

    private final StartSessionUseCase startSessionUseCase;
    private final CompleteSessionUseCase completeSessionUseCase;

    public TrainingController(
            StartSessionUseCase startSessionUseCase,
            CompleteSessionUseCase completeSessionUseCase) {
        this.startSessionUseCase = startSessionUseCase;
        this.completeSessionUseCase = completeSessionUseCase;
    }

    @PostMapping
    public ResponseEntity<StartSessionResult> startSession(@Valid @RequestBody(required = false) StartSessionRequest request) {
        StartSessionCommand cmd = new StartSessionCommand(
                request != null ? request.language() : "java",
                request != null ? request.mode() : "PRACTICE",
                request != null ? request.topic() : null,
                request != null ? request.difficulty() : null,
                request != null ? request.exerciseKey() : null,
                request != null ? request.duration() : null,
                request != null ? request.accuracyMaster() : null,
                request != null ? request.specialCategory() : null,
                request != null ? request.challengeId() : null,
                request != null ? request.competitionId() : null,
                request != null ? request.placement() : null,
                request != null && request.inputDevice() != null ? request.inputDevice() : "PHYSICAL",
                request != null && Boolean.TRUE.equals(request.strictIndent())
        );

        StartSessionResult result = startSessionUseCase.execute(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<CompleteSessionResult> completeSession(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CompleteSessionRequest request) {

        Double clientWpm = request.clientMetrics() != null ? request.clientMetrics().wpm() : null;
        Double clientAcc = request.clientMetrics() != null ? request.clientMetrics().accuracy() : null;

        CompleteSessionCommand cmd = new CompleteSessionCommand(
                id,
                request.completeToken(),
                request.keylog(),
                request.clientElapsedMs(),
                clientWpm,
                clientAcc
        );

        CompleteSessionResult result = completeSessionUseCase.execute(cmd);
        return ResponseEntity.ok(result);
    }
}
