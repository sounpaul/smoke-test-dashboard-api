package com.company.smoketestdashboard.controller;

import com.company.smoketestdashboard.model.STDashboardRequest;
import com.company.smoketestdashboard.model.STDashboardResponse;
import com.company.smoketestdashboard.model.TestResultResponse;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * @author Sounak Paul
 */

public interface STDashboardController {

    ResponseEntity<STDashboardResponse> addTestSuite(STDashboardRequest stDashboardRequest);
    ResponseEntity<Object> runTestSuite(String testSuiteName);
    ResponseEntity<STDashboardResponse> deleteTestSuite(long id);

}
