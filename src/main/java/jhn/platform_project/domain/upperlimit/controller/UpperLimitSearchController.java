package jhn.platform_project.domain.upperlimit.controller;

import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchRequest;
import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchResponse;
import jhn.platform_project.domain.upperlimit.service.UpperLimitSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/upper-limits")
@RequiredArgsConstructor
public class UpperLimitSearchController {

    private final UpperLimitSearchService upperLimitSearchService;

    @PostMapping("/search")
    public ResponseEntity<UpperLimitSearchResponse> search(@Valid @RequestBody UpperLimitSearchRequest request) {
        return ResponseEntity.ok(upperLimitSearchService.search(request));
    }
}