package com.rgs.bamboonotifier.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BambooResult {

    @JsonProperty("state")
    private String state;

    @JsonProperty("successfulTestCount")
    private int successfulTestCount;

    @JsonProperty("failedTestCount")
    private int failedTestCount;

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getSuccessfulTestCount() { return successfulTestCount; }
    public void setSuccessfulTestCount(int successfulTestCount) { this.successfulTestCount = successfulTestCount; }

    public int getFailedTestCount() { return failedTestCount; }
    public void setFailedTestCount(int failedTestCount) { this.failedTestCount = failedTestCount; }
}
