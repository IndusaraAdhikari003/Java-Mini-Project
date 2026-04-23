package com.example.javaminiproject.model;

import java.time.LocalTime;

public class Timetable {
    private int       ttId;
    private int       courseId;
    private String    dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String    location;
    private String    sessionType;
    private String    courseName;
    private String    courseCode;

    public Timetable(int ttId, int courseId, String dayOfWeek,
                     LocalTime startTime, LocalTime endTime,
                     String location, String sessionType) {
        this.ttId        = ttId;
        this.courseId    = courseId;
        this.dayOfWeek   = dayOfWeek;
        this.startTime   = startTime;
        this.endTime     = endTime;
        this.location    = location;
        this.sessionType = sessionType;
    }

    public int       getTtId()       { return ttId; }
    public int       getCourseId()   { return courseId; }
    public String    getDayOfWeek()  { return dayOfWeek; }
    public LocalTime getStartTime()  { return startTime; }
    public LocalTime getEndTime()    { return endTime; }
    public String    getLocation()   { return location; }
    public String    getSessionType(){ return sessionType; }
    public String    getCourseName() { return courseName; }
    public String    getCourseCode() { return courseCode; }

    public void setCourseName(String n) { this.courseName = n; }
    public void setCourseCode(String c) { this.courseCode = c; }
}