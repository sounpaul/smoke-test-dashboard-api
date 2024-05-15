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
    void saveTestResults(STDashboardRequest stDashboardRequest, List<TestStatusHistory> testStatusHistoryList, String testExecutionID);
    int executeTestSuite(String testSuiteName);
    TestSuiteResultResponse createTestResultResponse(
            int total, int passed, int failed, String startTime,
            String endTime, String duration, List<TestStatusHistory> testStatusHistoryList,
            String testSuiteName, String dashboardID);
    STDashboardRequest getTestSuite(String testSuiteName);
    int checkForRunReadiness(String featureFileName);
    STDashboardRequest updateTestSuite(STDashboardRequest stDashboardRequest);

}
