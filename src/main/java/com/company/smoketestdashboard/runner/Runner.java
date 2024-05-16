package com.company.smoketestdashboard.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(value = Cucumber.class)
@CucumberOptions(
        features = {"classpath:features"},
        glue = {"com.company.smoketestdashboard.stepdefinition"},
        monochrome = false,
        plugin = {"pretty", "json:build/reports/cucumber-reports/test-results.json"},
        tags = "@Test"
)
public class Runner {
}
