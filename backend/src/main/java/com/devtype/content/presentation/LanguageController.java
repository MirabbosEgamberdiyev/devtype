package com.devtype.content.presentation;

import com.devtype.content.application.ListLanguagesUseCase;
import com.devtype.content.application.PublishLanguageUseCase;
import com.devtype.content.presentation.ContentDtos.LanguageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LanguageController {

    private final ListLanguagesUseCase listLanguagesUseCase;
    private final PublishLanguageUseCase publishLanguageUseCase;

    public LanguageController(
            ListLanguagesUseCase listLanguagesUseCase,
            PublishLanguageUseCase publishLanguageUseCase) {
        this.listLanguagesUseCase = listLanguagesUseCase;
        this.publishLanguageUseCase = publishLanguageUseCase;
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LanguageResponse>> getLanguages() {
        return ResponseEntity.ok(listLanguagesUseCase.execute());
    }

    @PostMapping("/admin/languages/{slug}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> publishLanguage(@PathVariable String slug) {
        publishLanguageUseCase.execute(slug);
        return ResponseEntity.noContent().build();
    }
}
