package com.company.smoketestdashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestErrorResponse {
    private String testSuiteId;
    private String overallTestResult;
    private String testSuiteName;
    private String description;
}
