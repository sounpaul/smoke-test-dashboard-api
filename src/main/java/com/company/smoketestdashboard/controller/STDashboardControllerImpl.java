package com.company.smoketestdashboard.controller;

import com.company.smoketestdashboard.model.*;
import com.company.smoketestdashboard.service.STDashboardServiceImpl;
import com.company.smoketestdashboard.stepdefinition.GlobalHooks;
import com.company.smoketestdashboard.util.Constants;
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
public class STDashboardControllerImpl extends GlobalHooks implements STDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(STDashboardControllerImpl.class);

    @Autowired
    STDashboardServiceImpl stDashboardService;

    @Override
    @PostMapping(path = Constants.ADD_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<STDashboardResponse> addTestSuite(@RequestBody STDashboardRequest stDashboardRequest) {
        logger.info("Received request to add new test suite : {}", stDashboardRequest.toString());
        try {
            if (!(stDashboardRequest.getSuiteName() == null || stDashboardRequest.getFeatureFileName() == null)) {
                stDashboardRequest = stDashboardService.createTestSuite(stDashboardRequest);
                if (stDashboardRequest != null) {
                    logger.info("Successfully added test suite {}{}{}", "\"", stDashboardRequest.getSuiteName(), "\"");
                    return new ResponseEntity<STDashboardResponse>(new STDashboardResponse(Constants.TEST_SUITE_ADDED_STRING,
                            String.format("Test suite <%s> successfully added", stDashboardRequest.getSuiteName().trim())),
                            HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<STDashboardResponse>(new STDashboardResponse(Constants.FAILURE_STRING,
                            "Error saving test suite"),
                            HttpStatus.FAILED_DEPENDENCY);
                }
            } else {
                logger.error("Bad request : Null testSuitName or null featureFileName");
                return new ResponseEntity<STDashboardResponse>(new STDashboardResponse(Constants.FAILURE_STRING,
                        "Bad request : Null testSuitName or null featureFileName"),
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Exception caught : ", e);
            return new ResponseEntity<STDashboardResponse>(new STDashboardResponse(Constants.FAILURE_STRING,
                    String.format("Error adding test suite <%s>", stDashboardRequest.getSuiteName().trim())),
                    HttpStatus.ACCEPTED);
        }
    }

    @Override
    @PostMapping(path = Constants.RUN_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> runTestSuite(@RequestParam(name = "id") List<String> idList) {
        if (idList.size() > 1) {
            logger.info("Received request to run multiple test suites : Test Suite ID's={}", idList);
        }
        TestSuiteResultResponse testSuiteResultResponse = null;
        TestExecutionSummaryResponse testExecutionSummaryResponse = null;
        List<Object> testExecutionSummaryResponseList = new ArrayList<>();
        int exitStatus = 0;
        int runReadinessExitCode;
        try {
            for (String id : idList) {
                int pass = 0;
                int fail = 0;
                STDashboardRequest stDashboardRequest = stDashboardService.getTestSuiteById(Long.parseLong(id));
                String testSuiteName = stDashboardRequest.getSuiteName();
                logger.info("Received request to run test suite : TEST_SUITE_NAME={}, FEATURE_FILE_NAME={}.feature", testSuiteName, stDashboardRequest.getFeatureFileName());
                runReadinessExitCode = stDashboardService.checkForRunReadiness(stDashboardRequest.getFeatureFileName().trim());
                if (runReadinessExitCode == 0) {
                    logger.info("Glue code for feature file src/main/resources/features/{}.feature exists", stDashboardRequest.getFeatureFileName().trim());
                    exitStatus = stDashboardService.executeTestSuite(stDashboardRequest.getFeatureFileName());
                    stDashboardRequest.setStartTime(startTimeOverall);
                    stDashboardRequest.setEndTime(endTimeOverall);
                    stDashboardRequest.setDuration(overallDuration);
                    for (TestStatusHistory scenarioWiseStatus : testStatusHistoryList) {
                        if (scenarioWiseStatus.getStatus().equals("PASSED"))
                            pass++;
                        else if (scenarioWiseStatus.getStatus().equals("FAILED"))
                            fail++;
                    }
                    stDashboardRequest.setTotalTestCases(String.valueOf(testStatusHistoryList.size()));
                    stDashboardRequest.setTestCasesPassed(String.valueOf(pass));
                    stDashboardRequest.setTestCasesFailed(String.valueOf(fail));
                    testSuiteResultResponse = stDashboardService.createTestResultResponse(testStatusHistoryList.size(), pass, fail, startTimeOverall, endTimeOverall, overallDuration, testStatusHistoryList, testSuiteName, String.valueOf(stDashboardRequest.getId()));
                    String testResult = exitStatus == 0 ? "PASSED" : "FAILED";
                    stDashboardRequest.setTestResult(testResult);
                    testSuiteResultResponse.setOverallResult(testResult);
                    testExecutionSummaryResponseList.add(testSuiteResultResponse);
                    testExecutionSummaryResponse = new TestExecutionSummaryResponse();
                    stDashboardService.saveTestResults(stDashboardRequest, testStatusHistoryList, testExecutionID);
                    logger.info("Finished running test suite : TEST_SUITE_NAME={}, FEATURE_FILE_NAME={}.feature, TEST_RESULTS={}", testSuiteName, stDashboardRequest.getFeatureFileName(), testResult);
                } else if (runReadinessExitCode == -1) {
                    logger.error("Feature file does not exist in classpath");
                    testExecutionSummaryResponseList.add(new TestErrorResponse(
                            String.valueOf(stDashboardRequest.getId()),
                            testSuiteName,
                            String.format("Feature file %s.feature does not exist in classpath",
                                    stDashboardRequest.getFeatureFileName().trim())));
                } else if (runReadinessExitCode == 1) {
                    logger.error("Glue code does not exist in classpath");
                    testExecutionSummaryResponseList.add(new TestErrorResponse(
                            String.valueOf(stDashboardRequest.getId()),
                            testSuiteName,
                            String.format("Glue code for feature file %s.feature does not exist in classpath",
                                    stDashboardRequest.getFeatureFileName().trim())));
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught : ", e);
        }
        assert testExecutionSummaryResponse != null;
        testExecutionSummaryResponse.setTestExecutionSummary(testExecutionSummaryResponseList);
        return new ResponseEntity<>(testExecutionSummaryResponse, HttpStatus.ACCEPTED);
    }

    @Override
    @DeleteMapping(path = Constants.DELETE_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<STDashboardResponse> deleteTestSuite(@RequestParam(name = "id") long id) {
        STDashboardRequest stDashboardRequest = null;
        logger.info("Received request to delete existing test suite : TEST_SUITE_ID={}", id);
        try {
            stDashboardRequest = stDashboardService.purgeTestSuite(id).get();
            logger.info("Test suite {} deleted successfully", stDashboardRequest.getSuiteName());
            return new ResponseEntity<STDashboardResponse>(new STDashboardResponse(Constants.TEST_SUITE_DELETED_STRING,
                    String.format("Test suite <%s> deleted", stDashboardRequest.getSuiteName().trim())),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception caught : ", e);
            return new ResponseEntity<STDashboardResponse>(new STDashboardResponse(Constants.FAILURE_STRING,
                    String.format("Test suite with id=%s does not exist", id)),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @PostMapping(path = Constants.MODIFY_TEST_SUITE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<STDashboardResponse> updateTestSuite(String id,
                                                               @RequestParam(required = false, name = "testSuiteName") String testSuiteName,
                                                               @RequestParam(required = false, name = "featureFileName") String featureFileName,
                                                               @RequestParam(required = false, name = "isEnabled") String isEnabled) {
        try {
            logger.info("Received request to update test suite");
            STDashboardRequest stDashboardRequest = stDashboardService.getTestSuiteById(Long.parseLong(id));
            String oldVal = "";
            if (testSuiteName != null) {
                oldVal = stDashboardRequest.getSuiteName();
                stDashboardRequest.setSuiteName(testSuiteName);
                logger.info("Test suite name updated from [{}] to [{}]", oldVal, testSuiteName);
            }
            if (featureFileName != null) {
                oldVal = stDashboardRequest.getFeatureFileName();
                stDashboardRequest.setFeatureFileName(featureFileName);
                logger.info("Feature file name updated from [{}] to [{}]", oldVal, featureFileName);
            }
            if (isEnabled != null) {
                boolean val = stDashboardRequest.isEnabled();
                stDashboardRequest.setEnabled(!isEnabled.equalsIgnoreCase("no"));
                logger.info("Test suite isEnabled updated from [{}] to [{}]", val, !isEnabled.equalsIgnoreCase("no"));
            }
            if (!(featureFileName == null && testSuiteName == null && isEnabled == null)) {
                stDashboardService.updateTestSuite(stDashboardRequest);
                logger.info("Updated completed in database");
                return new ResponseEntity<>(new STDashboardResponse(Constants.MODIFIED_STRING,
                        "Test suite updated"), HttpStatus.ACCEPTED);
            } else {
                logger.error("Request params are null : testSuiteName={}, featureFileName={}, isEnabled={}", null, null, null);
                return new ResponseEntity<>(new STDashboardResponse(Constants.FAILURE_STRING, String.format("Request params are null : testSuiteName=%s, featureFileName=%s, isEnabled=%s", null, null, null)), HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (Exception e) {
            logger.error("Exception caught : ", e);
            return new ResponseEntity<>(new STDashboardResponse(Constants.FAILURE_STRING, "Test suite does not exist"), HttpStatus.NOT_FOUND);
        }
    }
}
