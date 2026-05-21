package com.rgs.bamboonotifier.DTO;

public class DeploymentInfo {

    private Long id;
    private String environmentId;
    private String deployVersion;
    private String status;
    private String startedDate;
    private String finishedDate;
    private String author;
    private String branchName;
    private String progressStatus;
    private String pinCode;
    private Long lastUpdated;
    private String buildState;
    private int testsPassed;
    private int testsFailed;
    private String duration;
    private String logUrl;
    private String triggerType;

    public String getLogUrl() { return logUrl; }
    public void setLogUrl(String logUrl) { this.logUrl = logUrl; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public Long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Long lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getBuildState() { return buildState; }
    public void setBuildState(String buildState) { this.buildState = buildState; }

    public int getTestsPassed() { return testsPassed; }
    public void setTestsPassed(int testsPassed) { this.testsPassed = testsPassed; }

    public int getTestsFailed() { return testsFailed; }
    public void setTestsFailed(int testsFailed) { this.testsFailed = testsFailed; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getDeployVersion() {
        return deployVersion;
    }

    public void setDeployVersion(String deployVersion) {
        this.deployVersion = deployVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(String startedDate) {
        this.startedDate = startedDate;
    }

    public String getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(String finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(String progressStatus) {
        this.progressStatus = progressStatus;
    }
}
