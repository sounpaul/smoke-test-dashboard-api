package com.company.smoketestdashboard.service;

import com.company.smoketestdashboard.model.HealthCheckResponse;
import com.company.smoketestdashboard.repository.STDashboardRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServiceImpl.class);

    @Autowired
    STDashboardRepository stDashboardRepository;
    @Override
    public HealthCheckResponse healthcheck() {
        HealthCheckResponse healthCheckResponse;
        if (getDBHealth()) {
            healthCheckResponse = new HealthCheckResponse("UP");
        } else {
            healthCheckResponse = new HealthCheckResponse("DOWN");
        }
        logger.info("DB Status : {}", healthCheckResponse.getDbStatus());
        return healthCheckResponse;
    }

    private boolean getDBHealth() {
        return stDashboardRepository.getHealth() == 1;
    }
}
