package com.company.smoketestdashboard.stepdefinition;

import com.company.smoketestdashboard.controller.STDashboardControllerImpl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStepDef {

    private static final Logger logger = LoggerFactory.getLogger(TestStepDef.class);

    int actualResult = 0;

    @Given("{string} and {string} is added")
    public void and_is_added(String num1, String num2) {
        actualResult = Integer.parseInt(num1) + Integer.parseInt(num2);
    }

    @Given("{string} and {string} is multiplied")
    public void and_is_multiplied(String num1, String num2) {
        actualResult = Integer.parseInt(num1) * Integer.parseInt(num2);
    }
    @Then("result should be {string}")
    public void result_should_be(String result) {
        Assert.assertEquals("Does not match",Integer.parseInt(result),actualResult);
    }

}
