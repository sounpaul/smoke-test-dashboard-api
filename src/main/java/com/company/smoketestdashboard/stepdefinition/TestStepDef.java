package com.company.smoketestdashboard.stepdefinition;

import com.company.smoketestdashboard.controller.STDashboardControllerImpl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.it.Ma;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class TestStepDef {

    String actualResult = "";
    String divisibility = "";

    @Given("{string} and {string} is added")
    public void and_is_added(String num1, String num2) {
        actualResult = String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    @Given("{string} and {string} is multiplied")
    public void and_is_multiplied(String num1, String num2) {
        actualResult = String.valueOf(Integer.parseInt(num1) * Integer.parseInt(num2));
    }

    @Then("result should be {string}")
    public void result_should_be(String result) {
        Assert.assertEquals("Does not match", result, actualResult);
    }

    @Given("{string} is subtracted from {string}")
    public void is_subtracted_from_is_subtracted(String secondNum, String firstNum) {
        actualResult = String.valueOf(Integer.parseInt(secondNum) - Integer.parseInt(firstNum));
    }

    @Given("{string} is divided by {string}")
    public void is_divided_by(String secondNum, String firstNum) {
        divisibility = (Integer.parseInt(secondNum) % Integer.parseInt(firstNum) == 0) ? "yes" : "no";
    }

    @Then("divisibility should be {string}")
    public void divisibility_should_be(String divisibilityStatus) {
        Assert.assertEquals("Does not match", divisibilityStatus, divisibility);
    }

}
