package com.rgs.bamboonotifier.DTO;

public class BuildInfo {
    private String state;
    private int testsPassed;
    private int testsFailed;

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getTestsPassed() { return testsPassed; }
    public void setTestsPassed(int testsPassed) { this.testsPassed = testsPassed; }

    public int getTestsFailed() { return testsFailed; }
    public void setTestsFailed(int testsFailed) { this.testsFailed = testsFailed; }
}
