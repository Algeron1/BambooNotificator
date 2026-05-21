package com.rgs.bamboonotifier.DTO;

public class UserInfo {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String status;
    private long createdAt;
    private boolean online;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}
