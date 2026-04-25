package com.example.javaminiproject.model;

import java.time.LocalDate;

public class Attendance {
    private int       attId;
    private int       ugId;
    private int       courseId;
    private LocalDate sessionDate;
    private String    sessionType; // THEORY or PRACTICAL
    private boolean   isPresent;

    // Extra display fields
    private String studentName;
    private String courseName;

    public Attendance(int attId, int ugId, int courseId,
                      LocalDate sessionDate, String sessionType, boolean isPresent) {
        this.attId       = attId;
        this.ugId        = ugId;
        this.courseId    = courseId;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.isPresent   = isPresent;
    }

    public int       getAttId()       { return attId; }
    public int       getUgId()        { return ugId; }
    public int       getCourseId()    { return courseId; }
    public LocalDate getSessionDate() { return sessionDate; }
    public String    getSessionType() { return sessionType; }
    public boolean   isPresent()      { return isPresent; }
    public String    getStudentName() { return studentName; }
    public String    getCourseName()  { return courseName; }

    public void setPresent(boolean p)      { this.isPresent  = p; }
    public void setStudentName(String s)   { this.studentName = s; }
    public void setCourseName(String c)    { this.courseName  = c; }
}