package com.rgs.bamboonotifier.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DeployResult {

    @JsonProperty("id")
    private long id;

    @JsonProperty("deploymentVersion")
    private DeploymentVersion deploymentVersion;

    @JsonProperty("startedDate")
    private Date startedDate;

    @JsonProperty("finishedDate")
    private Date finishedDate;

    @JsonProperty("deploymentState")
    private String deploymentState;

    @JsonProperty("lifeCycleState")
    private String lifeCycleState;

    @JsonProperty("reasonSummary")
    private String reasonSummary;

    @JsonProperty("logFiles")
    private List<String> logFiles;

    public DeploymentVersion getDeploymentVersion() {
        return deploymentVersion;
    }

    public void setDeploymentVersion(DeploymentVersion deploymentVersion) {
        this.deploymentVersion = deploymentVersion;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getDeploymentState() {
        return deploymentState;
    }

    public void setDeploymentState(String deploymentState) {
        this.deploymentState = deploymentState;
    }

    public String getReasonSummary() { return reasonSummary; }
    public void setReasonSummary(String reasonSummary) { this.reasonSummary = reasonSummary; }

    public List<String> getLogFiles() { return logFiles; }
    public void setLogFiles(List<String> logFiles) { this.logFiles = logFiles; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeploymentVersion {

        @JsonProperty("id")
        private long id;

        @JsonProperty("creatorDisplayName")
        private String creatorDisplayName;

        @JsonProperty("name")
        private String name;

        @JsonProperty("planBranchName")
        private String planBranchName;

        private String planResultKey;

        @JsonProperty("planResultKey")
        public void setPlanResultKey(Object value) {
            if (value instanceof String s) {
                this.planResultKey = s;
            } else if (value instanceof Map<?, ?> m) {
                Object key = m.get("key");
                if (key instanceof String s) this.planResultKey = s;
            }
        }

        public String getPlanResultKey() { return planResultKey; }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreatorDisplayName() {
            return creatorDisplayName;
        }

        public void setCreatorDisplayName(String creatorDisplayName) {
            this.creatorDisplayName = creatorDisplayName;
        }

        public String getPlanBranchName() {
            return planBranchName;
        }

        public void setPlanBranchName(String planBranchName) {
            this.planBranchName = planBranchName;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeployResult that = (DeployResult) o;
        return id == that.id && Objects.equals(startedDate, that.startedDate)
                && Objects.equals(finishedDate, that.finishedDate)
                && Objects.equals(deploymentState, that.deploymentState)
                && Objects.equals(lifeCycleState, that.lifeCycleState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deploymentVersion, startedDate, finishedDate, deploymentState);
    }

}
