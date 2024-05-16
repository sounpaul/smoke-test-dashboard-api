package com.company.smoketestdashboard.service;

import com.company.smoketestdashboard.model.STDashboardRequest;
import com.company.smoketestdashboard.model.TestSuiteResultResponse;
import com.company.smoketestdashboard.model.TestStatusHistory;

import java.util.List;
import java.util.Optional;

/**
 * @author Sounak Paul
 */
public interface STDashboardService {

    STDashboardRequest createTestSuite(STDashboardRequest stDashboardRequest);
    Optional<STDashboardRequest> purgeTestSuite(long id);
    void saveTestResults(STDashboardRequest stDashboardRequest);
    int executeTestSuite(String testSuiteName);
    TestSuiteResultResponse createTestResultResponse(String testSuiteName, String testSuiteID);
    STDashboardRequest getTestSuite(String testSuiteName);
    int checkForRunReadiness(String featureFileName);
    STDashboardRequest updateTestSuite(STDashboardRequest stDashboardRequest);

    STDashboardRequest captureDefinedTestExecutionResults(STDashboardRequest stDashboardRequest);
    STDashboardRequest captureUndefinedTestExecutionResults(STDashboardRequest stDashboardRequest, long startTimeInMills, String startTime);

}
