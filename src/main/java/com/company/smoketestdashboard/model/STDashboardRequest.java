package com.company.smoketestdashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sounak Paul
 */
@Entity
@Table(name = "Dashboard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class STDashboardRequest {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "DashboardSeqGen", sequenceName = "DashboardSequence", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DashboardSeqGen")
    private long id;
    @Column(name = "SUITE_NAME")
    private String suiteName;
    @Column(name = "LAST_UPDATED_TIME")
    private String lastUpdatedTime;
    @Column(name = "START_TIME")
    private String startTime;
    @Column(name = "END_TIME")
    private String endTime;
    private String duration;
    @Column(name = "FEATURE_FILE_NAME")
    private String featureFileName;
    @Column(name = "TEST_EXECUTION_ID")
    private String testExecutionID;
    @Column(name = "TEST_RESULT")
    private String testResult;
    @Column(name = "TOTAL_TEST_CASES")
    private String totalTestCases;
    @Column(name = "TEST_CASES_PASSED")
    private String testCasesPassed;
    @Column(name = "TEST_CASES_FAILED")
    private String testCasesFailed;
    @Column(name = "ENABLED")
    @JsonIgnore
    private boolean isEnabled;

    @Override
    public String toString() {
        return String.format("STDashboardRequest[suiteName=%s, featureFileName=%s]", this.suiteName, this.featureFileName);
    }

}
