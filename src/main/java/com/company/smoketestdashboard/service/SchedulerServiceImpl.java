package com.company.smoketestdashboard.service;

import com.company.smoketestdashboard.model.STDashboardRequest;
import com.company.smoketestdashboard.repository.STDashboardRepository;
import com.company.smoketestdashboard.stepdefinition.GlobalHooks;
import com.company.smoketestdashboard.util.Constants;
import com.company.smoketestdashboard.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Sounak Paul
 */
@Service
@Slf4j
public class SchedulerServiceImpl extends GlobalHooks implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Autowired
    STDashboardServiceImpl stDashboardService;

    @Autowired
    STDashboardRepository stDashboardRepository;

    @Override
    @Scheduled(cron = "${cron.expression}")
    public void runAllEnabledTestSuites() {
        logger.info("Starting execution of all enabled test suites as per cron schedule");
        try {
            int exitStatus = 0;
            int runReadinessExitCode = 0;
            String testResult = "";
            List<STDashboardRequest> testSuiteList = stDashboardRepository.findAllEnabledTestSuites();
            if (testSuiteList.size() > 0) {
                logger.info("Enabled test suites : {}", testSuiteList.stream().toList());
                for (STDashboardRequest stDashboardRequest : testSuiteList) {
                    String startTimeSrvcLvl = TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT);
                    long currentTimeInMillsSrvcLvl = System.currentTimeMillis();
                    runReadinessExitCode = stDashboardService.checkForRunReadiness(stDashboardRequest.getFeatureFileName().trim());
                    if (runReadinessExitCode == 0) {
                        logger.info("Glue code for feature file {}.feature exists", stDashboardRequest.getFeatureFileName().trim());
                        exitStatus = stDashboardService.executeTestSuite(stDashboardRequest.getFeatureFileName().trim());
                        stDashboardRequest = stDashboardService.captureDefinedTestExecutionResults(stDashboardRequest);
                        if (exitStatus == 0) {
                            testResult = Constants.TEST_CASE_PASSED_STRING;
                            stDashboardRequest.setTestResult(testResult);
                            stDashboardRequest.setNotes(String.format("All %s test cases passed", testStatusHistoryList.size()));
                        } else {
                            stDashboardRequest.setTestResult(Constants.FAILURE_STRING);
                            stDashboardRequest.setNotes(String.format("Failing test cases. Failed scenarios : %s", stDashboardService.getFailingScenarios()));
                            testResult = Constants.FAILURE_STRING;
                        }
                        stDashboardService.saveTestResults(stDashboardRequest);
                    } else {
                        String errorLogger = "";
                        stDashboardRequest = stDashboardService.captureUndefinedTestExecutionResults(stDashboardRequest,currentTimeInMillsSrvcLvl, startTimeSrvcLvl);
                        testResult = Constants.TEST_CASE_SKIPPED_STRING;
                        if (runReadinessExitCode == -1) {
                            errorLogger = String.format("Feature file %s.feature does not exist in classpath", stDashboardRequest.getFeatureFileName());
                        } else if (runReadinessExitCode == 1) {
                            errorLogger = String.format("Glue code for feature file %s.feature does not exist in classpath", stDashboardRequest.getFeatureFileName());
                        }
                        logger.error(errorLogger);
                        stDashboardRequest.setNotes(errorLogger);
                        stDashboardRepository.save(stDashboardRequest);
                    }
                    logger.info("Finished running test suite : TEST_SUITE_NAME={}, FEATURE_FILE_NAME={}.feature, TEST_RESULTS={}", stDashboardRequest.getSuiteName(), stDashboardRequest.getFeatureFileName(), testResult);
                }
            } else {
                logger.warn("No test suites are present in enabled state");
            }
        } catch (Exception e) {
            logger.error("Exception caught : ", e);
        }
    }
}
