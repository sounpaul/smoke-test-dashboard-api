package com.company.smoketestdashboard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Sounak Paul
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultResponse {
    private String overallResult;
    private int total;
    private int passed;
    private int failed;
    private String startTime;
    private String endTime;
    private String duration;
    private List<TestStatusHistory> scenarioWiseStatus;

}
