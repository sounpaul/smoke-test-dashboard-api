package com.company.smoketestdashboard.repository;

import com.company.smoketestdashboard.model.TestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestStatusHistoryRepository extends JpaRepository<TestStatusHistory, Long> {
}
