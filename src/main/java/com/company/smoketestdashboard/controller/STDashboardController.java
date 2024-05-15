package com.company.smoketestdashboard.controller;

import com.company.smoketestdashboard.model.STDashboardRequest;
import com.company.smoketestdashboard.model.STDashboardResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * @author Sounak Paul
 */

public interface STDashboardController {

    ResponseEntity<STDashboardResponse> addTestSuite(STDashboardRequest stDashboardRequest);
    ResponseEntity<Object> runTestSuite(List<String> idList);
    ResponseEntity<STDashboardResponse> deleteTestSuite(long id);
    ResponseEntity<STDashboardResponse> updateTestSuite(String id, String testSuiteName, String featureFileName, String isEnabled);

}
