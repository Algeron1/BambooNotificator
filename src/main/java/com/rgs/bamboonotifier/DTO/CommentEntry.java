package com.rgs.bamboonotifier.DTO;

public class CommentEntry {

    private String id;
    private String standName;
    private String authorId;
    private String authorName;
    private String text;
    private long createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStandName() { return standName; }
    public void setStandName(String standName) { this.standName = standName; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
