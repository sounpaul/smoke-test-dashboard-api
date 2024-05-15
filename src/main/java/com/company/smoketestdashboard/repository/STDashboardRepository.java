package com.company.smoketestdashboard.repository;

import com.company.smoketestdashboard.model.STDashboardRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface STDashboardRepository extends JpaRepository<STDashboardRequest, Long> {

    @Query("Select d from STDashboardRequest d where upper(d.suiteName) = ?1")
    STDashboardRequest findTestSuiteByName(String testSuiteName);
    @Query("Select d from STDashboardRequest d where d.isEnabled = true")
    List<STDashboardRequest> findAllEnabledTestSuites();


}
