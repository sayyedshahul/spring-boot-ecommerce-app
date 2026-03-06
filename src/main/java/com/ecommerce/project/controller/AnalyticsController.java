package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;


    @GetMapping("/api/admin/app/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalyticsData(){
        AnalyticsResponse analyticsResponse = analyticsService.getAnalyticsData();
        return new ResponseEntity<>(analyticsResponse, HttpStatus.OK);
    }
}
