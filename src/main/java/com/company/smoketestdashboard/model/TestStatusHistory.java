package com.company.smoketestdashboard.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TEST_RUN_HISTORY")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStatusHistory {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "TestHistorySeqGen", sequenceName = "TestHistorySequence", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TestHistorySeqGen")
    @JsonIgnore
    private long id;

    @Column(name = "SCENARIO_NAME")
    private String scenarioName;
    @Column(name = "TEST_CASE_ID")
    private String testCaseID;
    @Column(name = "STATUS")
    private String status;

    @JsonIgnore
    @Column(name = "TEST_EXECUTION_ID")
    private String testExecutionID;

    @Column(name = "START_TIME")
    private String startTime;
    @Column(name = "END_TIME")
    private String endTime;
    private String duration;

    public TestStatusHistory(String scenarioName, String testCaseID, String status) {
        this.scenarioName = scenarioName;
        this.testCaseID = testCaseID;
        this.status = status;
    }

}
