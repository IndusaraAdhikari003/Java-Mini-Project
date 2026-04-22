package com.example.javaminiproject.model;

public class Mark {
    private int    markId;
    private int    ugId;
    private int    courseId;
    private String examType;   // CA1, CA2, CA3, ASSIGNMENT, FINAL
    private double marksValue;
    private int    uploadedBy;
    private String studentName;
    private String courseName;

    public Mark(int markId, int ugId, int courseId, String examType,
                double marksValue, int uploadedBy) {
        this.markId     = markId;
        this.ugId       = ugId;
        this.courseId   = courseId;
        this.examType   = examType;
        this.marksValue = marksValue;
        this.uploadedBy = uploadedBy;
    }

    public int    getMarkId()      { return markId; }
    public int    getUgId()        { return ugId; }
    public int    getCourseId()    { return courseId; }
    public String getExamType()    { return examType; }
    public double getMarksValue()  { return marksValue; }
    public int    getUploadedBy()  { return uploadedBy; }
    public String getStudentName() { return studentName; }
    public String getCourseName()  { return courseName; }

    public void setMarksValue(double v)   { this.marksValue  = v; }
    public void setStudentName(String s)  { this.studentName = s; }
    public void setCourseName(String c)   { this.courseName  = c; }
}