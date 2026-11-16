package com.devtype.training.web;

import com.devtype.identity.api.GuestTokenVerifier;
import com.devtype.training.application.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class TrainingController {

    private final StartSessionUseCase startSessionUseCase;
    private final CompleteSessionUseCase completeSessionUseCase;
    private final NextExerciseUseCase nextExerciseUseCase;
    private final AbandonSessionUseCase abandonSessionUseCase;
    private final ClaimGuestSessionsUseCase claimGuestSessionsUseCase;
    private final SessionQueryService sessionQueryService;
    private final GuestTokenVerifier guestTokenVerifier;

    public TrainingController(
            StartSessionUseCase startSessionUseCase,
            CompleteSessionUseCase completeSessionUseCase,
            NextExerciseUseCase nextExerciseUseCase,
            AbandonSessionUseCase abandonSessionUseCase,
            ClaimGuestSessionsUseCase claimGuestSessionsUseCase,
            SessionQueryService sessionQueryService,
            GuestTokenVerifier guestTokenVerifier) {
        this.startSessionUseCase = startSessionUseCase;
        this.completeSessionUseCase = completeSessionUseCase;
        this.nextExerciseUseCase = nextExerciseUseCase;
        this.abandonSessionUseCase = abandonSessionUseCase;
        this.claimGuestSessionsUseCase = claimGuestSessionsUseCase;
        this.sessionQueryService = sessionQueryService;
        this.guestTokenVerifier = guestTokenVerifier;
    }

    public record NextSessionRequest(int afterSeq) {}
    public record ClaimRequest(String guestToken) {}

    @PostMapping
    public ResponseEntity<StartSessionResult> startSession(
            @Valid @RequestBody(required = false) StartSessionRequest request,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestTokenHeader,
            Authentication auth) {

        Long userId = resolveUserId(auth);
        UUID guestId = null;

        if (userId == null && guestTokenHeader != null) {
            guestId = guestTokenVerifier.verifyAndExtractGuestId(guestTokenHeader).orElse(null);
        }

        StartSessionCommand cmd = new StartSessionCommand(
                userId,
                guestId,
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

    @PostMapping("/{id}/next")
    public ResponseEntity<NextExerciseUseCase.NextExerciseResult> nextExercise(
            @PathVariable("id") UUID id,
            @RequestBody NextSessionRequest req) {
        NextExerciseUseCase.NextExerciseResult result = nextExerciseUseCase.execute(id, req.afterSeq());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<CompleteSessionResult> completeSession(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CompleteSessionRequest request,
            Authentication auth) {

        Long userId = resolveUserId(auth);
        Double clientWpm = request.clientMetrics() != null ? request.clientMetrics().wpm() : null;
        Double clientAcc = request.clientMetrics() != null ? request.clientMetrics().accuracy() : null;

        CompleteSessionCommand cmd = new CompleteSessionCommand(
                id,
                userId,
                request.completeToken(),
                request.keylog(),
                request.clientElapsedMs(),
                clientWpm,
                clientAcc
        );

        CompleteSessionResult result = completeSessionUseCase.execute(cmd);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/abandon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abandonSession(@PathVariable("id") UUID id) {
        abandonSessionUseCase.execute(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionQueryService.SessionDetailDto> getSession(@PathVariable("id") UUID id) {
        SessionQueryService.SessionDetailDto result = sessionQueryService.getSession(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/replay")
    public ResponseEntity<SessionQueryService.SessionReplayDto> getSessionReplay(@PathVariable("id") UUID id) {
        SessionQueryService.SessionReplayDto result = sessionQueryService.getSessionReplay(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<com.devtype.training.api.SessionQuery.SessionHistoryItemView>> getSessionsHistory(
            @RequestParam(name = "lang", required = false) String lang,
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "status", defaultValue = "COMPLETED") String status,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            Authentication auth
    ) {
        Long userId = resolveUserId(auth);
        if (userId == null) {
            throw new com.devtype.shared.error.DomainException("UNAUTHORIZED", "Avtorizatsiya talab qilinadi", HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(sessionQueryService.findSessions(userId, lang, mode, status, from, to, limit));
    }

    @PostMapping("/claim")
    public ResponseEntity<ClaimGuestSessionsUseCase.ClaimResult> claimSessions(
            @RequestBody ClaimRequest req,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        ClaimGuestSessionsUseCase.ClaimResult result = claimGuestSessionsUseCase.execute(userId, req.guestToken());
        return ResponseEntity.ok(result);
    }

    private Long resolveUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
