package com.company.smoketestdashboard.controller;

import com.company.smoketestdashboard.model.*;
import com.company.smoketestdashboard.service.HealthCheckServiceImpl;
import com.company.smoketestdashboard.service.STDashboardServiceImpl;
import com.company.smoketestdashboard.stepdefinition.GlobalHooks;
import com.company.smoketestdashboard.util.Constants;
import com.company.smoketestdashboard.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sounak Paul
 */

@RestController
@RequestMapping(value = Constants.CONTEXT_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class STDashboardControllerImpl implements STDashboardController {

    @Autowired
    STDashboardServiceImpl stDashboardService;
    @Autowired
    HealthCheckServiceImpl healthCheckService;

    @Override
    @PostMapping(path = Constants.ADD_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<STDashboardResponse> addTestSuite(@RequestBody STDashboardRequest stDashboardRequest) {
        log.info("Received request to add new test suite : {}", stDashboardRequest.toString());
        long currentTimeInMills = System.currentTimeMillis();
        try {
            if (!(stDashboardRequest.getSuiteName() == null || stDashboardRequest.getFeatureFileName() == null)) {
                STDashboardRequest dbData = stDashboardService.createTestSuite(stDashboardRequest);
                if (dbData != null) {
                    log.info("Successfully added test suite {}{}{}", "\"", stDashboardRequest.getSuiteName(), "\"");
                    log.info("Time taken to successfully process the ADD_TEST_SUITE request is {} ms ", System.currentTimeMillis() - currentTimeInMills);
                    return new ResponseEntity<>(new STDashboardResponse(Constants.TEST_SUITE_ADDED_STRING,
                            String.format("Test suite <%s> successfully added", stDashboardRequest.getSuiteName().trim())),
                            HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(new STDashboardResponse(Constants.FAILURE_STRING,
                            String.format("Test suite <%s> successfully added", stDashboardRequest.getSuiteName().trim())),
                            HttpStatus.NOT_MODIFIED);
                }
            } else {
                log.error("Bad request : Null testSuitName or null featureFileName");
                return new ResponseEntity<>(new STDashboardResponse(Constants.FAILURE_STRING,
                        "Bad request : Null testSuitName or null featureFileName"),
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Exception caught : ", e);
            return new ResponseEntity<>(new STDashboardResponse(Constants.FAILURE_STRING,
                    String.format("Error saving test suite <%s>", stDashboardRequest.getSuiteName().trim())),
                    HttpStatus.ACCEPTED);
        }
    }

    @Override
    @PostMapping(path = Constants.RUN_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> runTestSuite(@RequestParam(name = "id") List<String> idList) {
        if (idList.size() > 1) {
            log.info("Received request to run multiple test suites : Test Suite ID's={}", idList.stream().toList());
        }
        long currentTimeInMills;
        TestSuiteResultResponse testSuiteResultResponse = null;
        List<Object> testExecutionSummaryResponseList = new ArrayList<>();
        int exitStatus = 0;
        int runReadinessExitCode = 0;
        int notFoundCount = 0;
        for (String id : idList) {
            currentTimeInMills = System.currentTimeMillis();
            String testResult = "";
            try {
                STDashboardRequest stDashboardRequest = stDashboardService.getTestSuiteById(Long.parseLong(id));
                String testSuiteName = stDashboardRequest.getSuiteName();
                log.info("Received request to run test suite : TEST_SUITE_NAME={}, FEATURE_FILE_NAME={}.feature", testSuiteName, stDashboardRequest.getFeatureFileName());
                String startTimeSrvcLvl = TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT);
                long currentTimeInMillsSrvcLvl = System.currentTimeMillis();
                runReadinessExitCode = stDashboardService.checkForRunReadiness(stDashboardRequest.getFeatureFileName().trim());
                if (runReadinessExitCode == 0) {
                    log.info("Glue code for feature file {}.feature exists", stDashboardRequest.getFeatureFileName().trim());
                    exitStatus = stDashboardService.executeTestSuite(stDashboardRequest.getFeatureFileName().trim());
                    stDashboardRequest = stDashboardService.captureDefinedTestExecutionResults(stDashboardRequest);
                    testSuiteResultResponse = stDashboardService.createTestResultResponse(testSuiteName, id);
                    if (exitStatus == 0) {
                        testResult = Constants.TEST_CASE_PASSED_STRING;
                        stDashboardRequest.setTestResult(testResult);
                        testSuiteResultResponse.setOverallResult(testResult);
                        stDashboardRequest.setNotes(String.format("All %s test cases passed", stDashboardService.getTotal()));
                    } else {
                        testResult = Constants.FAILURE_STRING;
                        stDashboardRequest.setTestResult(testResult);
                        testSuiteResultResponse.setOverallResult(testResult);
                        stDashboardRequest.setNotes(String.format("Failing test cases. Failed scenarios : %s", stDashboardService.getFailingScenarios()));
                    }
                    stDashboardService.saveTestResults(stDashboardRequest);
                    testExecutionSummaryResponseList.add(testSuiteResultResponse);
                } else {
                    String errorLogger = "";
                    testResult = Constants.TEST_CASE_SKIPPED_STRING;
                    stDashboardRequest = stDashboardService.captureUndefinedTestExecutionResults(stDashboardRequest, currentTimeInMillsSrvcLvl, startTimeSrvcLvl);
                    if (runReadinessExitCode == -1) {
                        errorLogger = String.format("Feature file %s.feature does not exist in classpath", stDashboardRequest.getFeatureFileName());
                    } else if (runReadinessExitCode == 1) {
                        errorLogger = String.format("Glue code for feature file %s.feature does not exist in classpath", stDashboardRequest.getFeatureFileName());
                    }
                    testExecutionSummaryResponseList.add(new TestErrorResponse(String.valueOf(stDashboardRequest.getId()), Constants.TEST_CASE_SKIPPED_STRING, testSuiteName, errorLogger));
                    log.error(errorLogger);
                    stDashboardRequest.setNotes(errorLogger);
                    stDashboardService.updateTestSuite(stDashboardRequest);
                }
                log.info("Time taken to successfully process the RUN_TEST_SUITE request is {} ms ", System.currentTimeMillis() - currentTimeInMills);
                log.info("Finished running test suite : TEST_SUITE_NAME={}, FEATURE_FILE_NAME={}.feature, TEST_RESULTS={}", stDashboardRequest.getSuiteName(), stDashboardRequest.getFeatureFileName(), testResult);
            } catch (Exception e) {
                log.error("Exception caught : ", e);
                notFoundCount++;
                testExecutionSummaryResponseList.add(new CustomErrorResponse(String.format("Test suite with id=%s does not exist", id)));
            }
        }
        return new ResponseEntity<>(testExecutionSummaryResponseList, idList.size() == notFoundCount ? HttpStatus.NOT_FOUND : HttpStatus.ACCEPTED);
    }

    @Override
    @DeleteMapping(path = Constants.DELETE_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteTestSuite(@RequestParam(name = "id") List<String> idList) {
        STDashboardRequest stDashboardRequest = null;
        int suiteNotPresent = 0;
        List<STDashboardResponse> stDashboardResponseList = new ArrayList<>();
        long currentTimeInMills;
        if (idList.size() > 1) {
            log.info("Received request to delete multiple test suites from dashboard : Test Suite ID's - {}", idList.stream().toList());
        }
        for (String id : idList) {
            currentTimeInMills = System.currentTimeMillis();
            log.info("Received request to delete existing test suite : TEST_SUITE_ID={}", id);
            try {
                stDashboardRequest = stDashboardService.purgeTestSuite(Long.parseLong(id)).get();
                log.info("Test suite {} deleted successfully", stDashboardRequest.getSuiteName());
                stDashboardResponseList.add(new STDashboardResponse(Constants.TEST_SUITE_DELETED_STRING, String.format("Test suite <%s> deleted", stDashboardRequest.getSuiteName().trim())));
                log.info("Time taken to successfully process the DELETE_TEST_SUITE request is {} ms ", System.currentTimeMillis() - currentTimeInMills);
            } catch (Exception e) {
                log.error("Exception caught : ", e);
                suiteNotPresent++;
                stDashboardResponseList.add(new STDashboardResponse(Constants.FAILURE_STRING, String.format("Test suite with id=%s does not exist", id)));
            }
        }
        return new ResponseEntity<>(stDashboardResponseList, suiteNotPresent == idList.size() ? HttpStatus.NOT_FOUND : HttpStatus.ACCEPTED);
    }

    @Override
    @PostMapping(path = Constants.MODIFY_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<STDashboardResponse> updateTestSuite(String id,
                                                               @RequestParam(required = false, name = "testSuiteName") String testSuiteName,
                                                               @RequestParam(required = false, name = "featureFileName") String featureFileName,
                                                               @RequestParam(required = false, name = "isEnabled") String isEnabled) {
        try {
            log.info("Received request to update test suite");
            STDashboardRequest stDashboardRequest = stDashboardService.getTestSuiteById(Long.parseLong(id));
            String oldVal = "";
            long currentTimeInMills = System.currentTimeMillis();
            if (testSuiteName != null) {
                oldVal = stDashboardRequest.getSuiteName();
                stDashboardRequest.setSuiteName(testSuiteName);
                log.info("Test suite name updated from [{}] to [{}]", oldVal, testSuiteName);
            }
            if (featureFileName != null) {
                oldVal = stDashboardRequest.getFeatureFileName();
                stDashboardRequest.setFeatureFileName(featureFileName);
                log.info("Feature file name updated from [{}] to [{}]", oldVal, featureFileName);
            }
            if (isEnabled != null) {
                boolean val = stDashboardRequest.isEnabled();
                stDashboardRequest.setEnabled(!isEnabled.equalsIgnoreCase("no"));
                log.info("Test suite isEnabled updated from [{}] to [{}]", val, !isEnabled.equalsIgnoreCase("no"));
            }
            if (!(featureFileName == null && testSuiteName == null && isEnabled == null)) {
                stDashboardService.updateTestSuite(stDashboardRequest);
                log.info("Updated completed in database");
                log.info("Time taken to successfully process the MODIFY_TEST_SUITE request is {} ms ", System.currentTimeMillis() - currentTimeInMills);
                return new ResponseEntity<>(new STDashboardResponse(Constants.TEST_SUITE_MODIFIED_STRING,
                        "Test suite updated"), HttpStatus.ACCEPTED);
            } else {
                log.error("Request params are null : testSuiteName={}, featureFileName={}, isEnabled={}", null, null, null);
                return new ResponseEntity<>(new STDashboardResponse(Constants.FAILURE_STRING, String.format("Request params are null : testSuiteName=%s, featureFileName=%s, isEnabled=%s", null, null, null)), HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (Exception e) {
            log.error("Exception caught : ", e);
            return new ResponseEntity<>(new STDashboardResponse(Constants.FAILURE_STRING, "Test suite does not exist"), HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @GetMapping(path = Constants.HEALTH_CHECK, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthCheckResponse> healthcheck() {
        long currentTimeInMills = System.currentTimeMillis();
        log.info("Request received to perform healthcheck on database");
        HealthCheckResponse healthCheckResponse = healthCheckService.healthcheck();
        log.info("Healthcheck performed in {} ms", System.currentTimeMillis() - currentTimeInMills);
        return new ResponseEntity<>(healthCheckResponse, healthCheckResponse.getDbStatus().equals("UP") ? HttpStatus.OK : HttpStatus.EXPECTATION_FAILED);
    }
}
