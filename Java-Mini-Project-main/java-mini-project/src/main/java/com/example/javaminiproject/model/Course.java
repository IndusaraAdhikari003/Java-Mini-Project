package com.example.javaminiproject.model;

public class Course {
    private int     courseId;
    private String  courseCode;
    private String  courseName;
    private int     credits;
    private boolean hasTheory;
    private boolean hasPractical;
    private String  department;
    private int     lecturerId;
    private String  lecturerName;

    public Course(int courseId, String courseCode, String courseName,
                  int credits, boolean hasTheory, boolean hasPractical,
                  String department, int lecturerId, String lecturerName) {
        this.courseId    = courseId;
        this.courseCode  = courseCode;
        this.courseName  = courseName;
        this.credits     = credits;
        this.hasTheory   = hasTheory;
        this.hasPractical = hasPractical;
        this.department  = department;
        this.lecturerId  = lecturerId;
        this.lecturerName = lecturerName;
    }

    public int     getCourseId()    { return courseId; }
    public String  getCourseCode()  { return courseCode; }
    public String  getCourseName()  { return courseName; }
    public int     getCredits()     { return credits; }
    public boolean isHasTheory()    { return hasTheory; }
    public boolean isHasPractical() { return hasPractical; }
    public String  getDepartment()  { return department; }
    public int     getLecturerId()  { return lecturerId; }
    public String  getLecturerName(){ return lecturerName; }

    public void setCourseCode(String c)  { this.courseCode  = c; }
    public void setCourseName(String n)  { this.courseName  = n; }
    public void setCredits(int cr)       { this.credits     = cr; }
    public void setLecturerId(int id)    { this.lecturerId  = id; }

    @Override
    public String toString() { return courseCode + " - " + courseName; }
}