package com.devtype.content.presentation;

import com.devtype.content.application.CreateExerciseUseCase;
import com.devtype.content.application.GetExerciseUseCase;
import com.devtype.content.application.PublishExerciseUseCase;
import com.devtype.content.presentation.ContentDtos.CreateExerciseRequest;
import com.devtype.content.presentation.ContentDtos.ExerciseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ExerciseController {

    private final GetExerciseUseCase getExerciseUseCase;
    private final CreateExerciseUseCase createExerciseUseCase;
    private final PublishExerciseUseCase publishExerciseUseCase;

    public ExerciseController(
            GetExerciseUseCase getExerciseUseCase,
            CreateExerciseUseCase createExerciseUseCase,
            PublishExerciseUseCase publishExerciseUseCase) {
        this.getExerciseUseCase = getExerciseUseCase;
        this.createExerciseUseCase = createExerciseUseCase;
        this.publishExerciseUseCase = publishExerciseUseCase;
    }

    @GetMapping("/exercises/{key}")
    public ResponseEntity<ExerciseResponse> getExercise(@PathVariable String key) {
        return ResponseEntity.ok(getExerciseUseCase.execute(UUID.fromString(key)));
    }

    @PostMapping("/admin/exercises")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ExerciseResponse> createExercise(
            @Valid @RequestBody CreateExerciseRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        ExerciseResponse response = createExerciseUseCase.execute(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/exercises/{key}/publish")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ExerciseResponse> publishExercise(
            @PathVariable String key,
            @RequestParam(defaultValue = "1") int version,
            @AuthenticationPrincipal Long currentUserId) {
        ExerciseResponse response = publishExerciseUseCase.execute(UUID.fromString(key), version, currentUserId);
        return ResponseEntity.ok(response);
    }
}
