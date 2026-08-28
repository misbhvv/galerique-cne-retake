package com.group2.backend.controller;

import com.group2.backend.dto.TagSuggestionDto;
import com.group2.backend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@Tag(name = "tags", description = "Tag suggestions and lookup")
public class TagController {

    private final TagService tagService;

    @GetMapping("/suggestions")
    @Operation(summary = "Suggest tags", description = "Returns reusable tags and synonym matches")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<List<TagSuggestionDto>> suggestions(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(tagService.suggestTags(query));
    }

    @GetMapping("/popular")
    @Operation(summary = "Popular tags", description = "Returns most used tags")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<List<TagSuggestionDto>> popular(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(tagService.popularTags(limit));
    }
}
