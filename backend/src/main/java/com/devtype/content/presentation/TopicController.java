package com.devtype.content.presentation;

import com.devtype.content.application.GetLanguageTopicsUseCase;
import com.devtype.content.presentation.ContentDtos.TopicResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/languages/{slug}/topics")
public class TopicController {

    private final GetLanguageTopicsUseCase getLanguageTopicsUseCase;

    public TopicController(GetLanguageTopicsUseCase getLanguageTopicsUseCase) {
        this.getLanguageTopicsUseCase = getLanguageTopicsUseCase;
    }

    @GetMapping
    public ResponseEntity<List<TopicResponse>> getTopics(@PathVariable String slug) {
        return ResponseEntity.ok(getLanguageTopicsUseCase.execute(slug));
    }
}
