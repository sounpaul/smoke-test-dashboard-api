package com.company.smoketestdashboard.service;

import com.company.smoketestdashboard.exception.SMDashboardException;
import com.company.smoketestdashboard.model.STDashboardRequest;
import com.company.smoketestdashboard.model.TestSuiteResultResponse;
import com.company.smoketestdashboard.model.TestStatusHistory;
import com.company.smoketestdashboard.repository.STDashboardRepository;
import com.company.smoketestdashboard.repository.TestStatusHistoryRepository;
import com.company.smoketestdashboard.util.Constants;
import com.company.smoketestdashboard.util.TimeUtils;
import io.cucumber.core.cli.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Sounak Paul
 */
@Service
public class STDashboardServiceImpl implements STDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(STDashboardServiceImpl.class);

    @Autowired
    STDashboardRepository stDashboardRepository;
    @Autowired
    TestStatusHistoryRepository testStatusHistoryRepository;

    @Override
    public STDashboardRequest createTestSuite(STDashboardRequest stDashboardRequest) {
        logger.info("Adding test suite \"{}\"", stDashboardRequest.getSuiteName());
        STDashboardRequest dashboardDBData = null;
        try {
            dashboardDBData = stDashboardRepository.findTestSuiteByName(transform(stDashboardRequest, "ADD").getSuiteName().toUpperCase().trim());
            if (dashboardDBData == null) {
                dashboardDBData = stDashboardRepository.save(stDashboardRequest);
            } else {
                throw new SMDashboardException("Test suite name \"" + stDashboardRequest.getSuiteName().trim() + "\"" + " already exists");
            }
        } catch (Exception e) {
            logger.error("Exception caught : ", e);
        }
        return dashboardDBData;
    }

    @Override
    public Optional<STDashboardRequest> purgeTestSuite(long id) {
        Optional<STDashboardRequest> stDashboardRequest = null;
        if (stDashboardRepository.existsById(id)) {
            logger.info("Test suite found with TEST_SUITE_ID={} in database", id);
            stDashboardRequest = stDashboardRepository.findById(id);
            stDashboardRepository.deleteById(id);
            return stDashboardRequest;
        } else {
            throw new SMDashboardException(String.format("Test suite with TEST_SUITE_ID=%s does not exist", id));
        }
    }

    @Override
    public void saveTestResults(STDashboardRequest stDashboardRequest, List<TestStatusHistory> testStatusHistoryList, String testExecutionId) {
        stDashboardRequest.setTestExecutionID(testExecutionId);
        stDashboardRepository.save(stDashboardRequest);
        for (TestStatusHistory testStatusHistory : testStatusHistoryList) {
            testStatusHistory.setTestExecutionID(testExecutionId);
            testStatusHistory.setTestSuiteID(stDashboardRequest.getId());
            testStatusHistoryRepository.save(testStatusHistory);
        }
    }

    @Override
    public int executeTestSuite(String featureFileName) {
        String[] args = new String[]{"-g", "com.company.smoketestdashboard.stepdefinition",
                String.format("./src/main/resources/features/%s.feature", featureFileName.trim())};
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return Main.run(args, contextClassLoader);

    }

    @Override
    public TestSuiteResultResponse createTestResultResponse(int total, int passed, int failed, String startTime, String endTime, String duration, List<TestStatusHistory> testStatusHistoryList, String testSuiteName, String testSuiteID) {
        TestSuiteResultResponse testSuiteResultResponse = new TestSuiteResultResponse();
        testSuiteResultResponse.setPassed(passed);
        testSuiteResultResponse.setFailed(failed);
        testSuiteResultResponse.setTotal(total);
        testSuiteResultResponse.setStartTime(startTime);
        testSuiteResultResponse.setEndTime(endTime);
        testSuiteResultResponse.setDuration(duration);
        testSuiteResultResponse.setScenarioWiseStatus(testStatusHistoryList);
        testSuiteResultResponse.setTestSuiteName(testSuiteName);
        testSuiteResultResponse.setTestSuiteID(testSuiteID);
        return testSuiteResultResponse;
    }

    @Override
    public STDashboardRequest getTestSuite(String testSuiteName) {
        return stDashboardRepository.findTestSuiteByName(testSuiteName);
    }

    public STDashboardRequest getTestSuiteById(long id) {
        STDashboardRequest dbData = null;
        if (stDashboardRepository.existsById(id)) {
            dbData = stDashboardRepository.findById(id).get();
        } else {
            throw new SMDashboardException(String.format("Test suite with id=%s does not exist", id));
        }
        return dbData;
    }

    @Override
    public int checkForRunReadiness(String featureFileName) {
        int returnCode = -1;
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader.getResource("features" + System.getProperty("file.separator") + String.format("%s.feature", featureFileName)) != null) {
            logger.info("Feature file {}.feature exists", featureFileName);
            String[] args = new String[]{"-g", "com.company.smoketestdashboard.stepdefinition",
                    String.format("./src/main/resources/features/%s.feature", featureFileName.trim()),
                    "--dry-run"};
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            returnCode = Main.run(args, contextClassLoader);
        }
        return returnCode;
    }

    @Override
    public STDashboardRequest updateTestSuite(STDashboardRequest stDashboardRequest) {
        if (stDashboardRepository.existsById(stDashboardRequest.getId())) {
            stDashboardRequest = stDashboardRepository.save(stDashboardRequest);
        } else {
            throw new SMDashboardException(String.format("Test suite with id=%s does not exist", stDashboardRequest.getId()));
        }
        return stDashboardRequest;
    }

    private static STDashboardRequest transform(STDashboardRequest stDashboardRequest, String action) {
        stDashboardRequest.setStartTime(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setEndTime(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setDuration(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setTestResult(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setTotalTestCases(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setTestCasesPassed(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setTestCasesFailed(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setTestExecutionID(Constants.NOT_YET_EXECUTED);
        stDashboardRequest.setLastUpdatedTime(TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT));
        stDashboardRequest.setEnabled(true);
        return stDashboardRequest;
    }

}
