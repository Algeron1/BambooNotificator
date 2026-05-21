package com.rgs.bamboonotifier.DTO;

public class DeployHistoryEntry {

    private long timestamp;
    private String state;
    private String lifeCycleState;
    private String version;
    private String branch;
    private String author;

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getLifeCycleState() { return lifeCycleState; }
    public void setLifeCycleState(String lifeCycleState) { this.lifeCycleState = lifeCycleState; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
