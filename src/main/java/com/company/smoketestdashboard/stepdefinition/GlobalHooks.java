package com.company.smoketestdashboard.stepdefinition;

import com.company.smoketestdashboard.model.TestStatusHistory;
import com.company.smoketestdashboard.util.Constants;
import com.company.smoketestdashboard.util.TimeUtils;
import io.cucumber.java.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Slf4j
public class GlobalHooks {

    private static final Logger logger = LoggerFactory.getLogger(GlobalHooks.class);
    public static List<TestStatusHistory> testStatusHistoryList;
    private TestStatusHistory scenarioWiseStatus;
    private long startTimeMillis;
    public static String startTimeOverall;
    public static String endTimeOverall;
    private static long startTimeOverallMillis;
    public static String overallDuration;
    public static String testExecutionID;

    @BeforeAll
    public static void beforeAllTest() {
        startTimeOverall = TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT);
        startTimeOverallMillis = System.currentTimeMillis();
        testStatusHistoryList = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        testExecutionID = uuid.toString();
    }

    @Before
    public void beforeEachTest(Scenario scenario) {
        scenarioWiseStatus = new TestStatusHistory();
        startTimeMillis = System.currentTimeMillis();
        scenarioWiseStatus.setStartTime(TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT));
        logger.info("Starting tests for scenario : SCENARIO_NAME=[{}], TEST_EXECUTION_ID=[{}], TEST_CASE_ID=[{}]",
                scenario.getName(), testExecutionID, scenario.getId());
    }

    @After
    public void afterEachTest(Scenario scenario) {
        scenarioWiseStatus.setEndTime(TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT));
        scenarioWiseStatus.setDuration(String.valueOf(System.currentTimeMillis() - startTimeMillis));
        scenarioWiseStatus.setScenarioName(scenario.getName());
        scenarioWiseStatus.setTestCaseID(scenario.getId());
        scenarioWiseStatus.setStatus(scenario.getStatus().name());
        testStatusHistoryList.add(scenarioWiseStatus);
        logger.info("Finished testing for scenario : SCENARIO_NAME=[{}], TEST_EXECUTION_ID=[{}], TEST_CASE_ID=[{}], STATUS=[{}]",
                scenario.getName(), testExecutionID, scenario.getId(), scenario.getStatus().name());
    }

    @AfterAll
    public static void afterAllTest() {
        endTimeOverall = TimeUtils.getCurrentDateTime(Constants.DATETIME_FORMAT);
        overallDuration = String.valueOf(System.currentTimeMillis() - startTimeOverallMillis);
    }

}
