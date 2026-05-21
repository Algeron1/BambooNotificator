package com.rgs.bamboonotifier.DTO;

public class QueueItem {

    private String environmentId;
    private String standName;
    private String systemName;
    private String version;
    private String author;
    private String branch;

    public String getEnvironmentId() { return environmentId; }
    public void setEnvironmentId(String environmentId) { this.environmentId = environmentId; }

    public String getStandName() { return standName; }
    public void setStandName(String standName) { this.standName = standName; }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
}
