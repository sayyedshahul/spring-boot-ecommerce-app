package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Analytics API")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @Operation(summary = "Get Analytics related data")
    @GetMapping("/api/admin/app/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalyticsData(){
        AnalyticsResponse analyticsResponse = analyticsService.getAnalyticsData();
        return new ResponseEntity<>(analyticsResponse, HttpStatus.OK);
    }
}
