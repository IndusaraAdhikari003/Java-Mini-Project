package com.example.javaminiproject.model;

import java.time.LocalDateTime;

public class Notice {
    private int           noticeId;
    private String        title;
    private String        content;
    private int           createdBy;
    private LocalDateTime createdAt;
    private String        createdByName;

    public Notice(int noticeId, String title, String content,
                  int createdBy, LocalDateTime createdAt) {
        this.noticeId  = noticeId;
        this.title     = title;
        this.content   = content;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public int           getNoticeId()     { return noticeId; }
    public String        getTitle()        { return title; }
    public String        getContent()      { return content; }
    public int           getCreatedBy()    { return createdBy; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public String        getCreatedByName(){ return createdByName; }

    public void setTitle(String t)          { this.title         = t; }
    public void setContent(String c)        { this.content       = c; }
    public void setCreatedByName(String n)  { this.createdByName = n; }
}