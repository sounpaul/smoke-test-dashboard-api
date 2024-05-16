package com.company.smoketestdashboard.service;

import com.company.smoketestdashboard.exception.SMDashboardException;
import com.company.smoketestdashboard.model.STDashboardRequest;
import com.company.smoketestdashboard.model.TestSuiteResultResponse;
import com.company.smoketestdashboard.model.TestStatusHistory;
import com.company.smoketestdashboard.repository.STDashboardRepository;
import com.company.smoketestdashboard.repository.TestStatusHistoryRepository;
import com.company.smoketestdashboard.stepdefinition.GlobalHooks;
import com.company.smoketestdashboard.util.Constants;
import com.company.smoketestdashboard.util.TimeUtils;
import io.cucumber.core.cli.Main;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Sounak Paul
 */
@Service
@Getter
@Setter
public class STDashboardServiceImpl extends GlobalHooks implements STDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(STDashboardServiceImpl.class);

    @Autowired
    STDashboardRepository stDashboardRepository;
    @Autowired
    TestStatusHistoryRepository testStatusHistoryRepository;
    private int pass;
    private int fail;
    private int total;
    private List<String> failingScenarios;

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
    public void saveTestResults(STDashboardRequest stDashboardRequest) {
        stDashboardRequest.setTestExecutionID(testExecutionID);
        stDashboardRepository.save(stDashboardRequest);
        for (TestStatusHistory testStatusHistory : testStatusHistoryList) {
            testStatusHistory.setTestExecutionID(testExecutionID);
            testStatusHistory.setTestSuiteID(stDashboardRequest.getId());
            testStatusHistoryRepository.save(testStatusHistory);
        }
        logger.info("Test results saved to DASHBOARD & TEST_RUN_HISTORY tables");
    }

    @Override
    public int checkForRunReadiness(String featureFileName) {
        int returnCode = -1;
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = contextClassLoader.getResourceAsStream(String.format("%s.feature", featureFileName));
            if (is != null) {
                URL featureFileResource = this.getClass().getClassLoader().getResource(String.format("%s.feature", featureFileName));
                logger.info("Feature file {}.feature exists", featureFileName);
                String[] args = new String[]{"-g", "com.company.smoketestdashboard.stepdefinition", featureFileResource.toExternalForm(), "--dry-run"};
                returnCode = Main.run(args, contextClassLoader);
            }
        } catch (Exception e) {
            logger.error("Exception caught : ", e);
        }
        return returnCode;
    }

    @Override
    public int executeTestSuite(String featureFileName) {
        int exitCode = 1;
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = contextClassLoader.getResourceAsStream(String.format("%s.feature", featureFileName));
            if (is != null) {
                URL featureFileResource = this.getClass().getClassLoader().getResource(String.format("%s.feature", featureFileName));
                logger.info("Feature file {}.feature exists", featureFileName);
                String[] args = new String[]{"-g", "com.company.smoketestdashboard.stepdefinition", featureFileResource.toExternalForm()};
                exitCode = Main.run(args, contextClassLoader);
            }
            logger.info("Cucumber execution completed, exit code={}", exitCode);
        } catch (Exception e) {
            logger.error("Exception e : ", e);
        }
        return exitCode;

    }

    @Override
    public TestSuiteResultResponse createTestResultResponse(String testSuiteName, String testSuiteID) {
        TestSuiteResultResponse testSuiteResultResponse = new TestSuiteResultResponse();
        testSuiteResultResponse.setPassed(pass);
        testSuiteResultResponse.setFailed(fail);
        testSuiteResultResponse.setTotal(testStatusHistoryList.size());
        testSuiteResultResponse.setStartTime(startTimeOverall);
        testSuiteResultResponse.setEndTime(endTimeOverall);
        testSuiteResultResponse.setDuration(overallDuration);
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
    public STDashboardRequest updateTestSuite(STDashboardRequest stDashboardRequest) {
        if (stDashboardRepository.existsById(stDashboardRequest.getId())) {
            stDashboardRequest.setLastUpdatedTime(TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT));
            stDashboardRequest = stDashboardRepository.save(stDashboardRequest);
        } else {
            throw new SMDashboardException(String.format("Test suite with id=%s does not exist", stDashboardRequest.getId()));
        }
        return stDashboardRequest;
    }

    @Override
    public STDashboardRequest captureDefinedTestExecutionResults(STDashboardRequest stDashboardRequest) {
        this.pass = 0;
        this.fail = 0;
        stDashboardRequest.setStartTime(startTimeOverall);
        stDashboardRequest.setEndTime(endTimeOverall);
        stDashboardRequest.setDuration(overallDuration);
        for (TestStatusHistory scenarioWiseStatus : testStatusHistoryList) {
            if (scenarioWiseStatus.getStatus().equals("PASSED")) {
                this.pass++;
            } else if (scenarioWiseStatus.getStatus().equals("FAILED")) {
                this.fail++;
                this.failingScenarios = new ArrayList<>();
                failingScenarios.add(scenarioWiseStatus.getScenarioName());
            }
        }
        this.total = testStatusHistoryList.size();
        stDashboardRequest.setTotalTestCases(String.valueOf(total));
        stDashboardRequest.setTestCasesPassed(String.valueOf(pass));
        stDashboardRequest.setTestCasesFailed(String.valueOf(fail));
        stDashboardRequest.setTestExecutionID(testExecutionID);
        return stDashboardRequest;
    }

    @Override
    public STDashboardRequest captureUndefinedTestExecutionResults(STDashboardRequest stDashboardRequest, long startTimeInMills, String startTime) {
        String endTimeSrvcLvl = TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT);
        stDashboardRequest.setDuration(String.valueOf(System.currentTimeMillis() - startTimeInMills));
        stDashboardRequest.setStartTime(startTime);
        stDashboardRequest.setEndTime(endTimeSrvcLvl);
        stDashboardRequest.setTestResult(Constants.TEST_CASE_SKIPPED_STRING);
        stDashboardRequest.setTotalTestCases(Constants.TEST_UNDEFINED_STRING);
        stDashboardRequest.setTestCasesPassed(Constants.TEST_UNDEFINED_STRING);
        stDashboardRequest.setTestCasesFailed(Constants.TEST_UNDEFINED_STRING);
        stDashboardRequest.setTestExecutionID(Constants.TEST_UNDEFINED_STRING);
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
