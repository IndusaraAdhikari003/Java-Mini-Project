package com.example.javaminiproject.model;

public class CourseMaterial {
    private int materialId;
    private int courseId;
    private String title;
    private String filePath;
    private int uploadedBy;
    private String courseName;

    public CourseMaterial(int materialId, int courseId, String title, String filePath, int uploadedBy) {
        this.materialId = materialId;
        this.courseId = courseId;
        this.title = title;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
    }

    public int getMaterialId() {
        return materialId;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    @Override
    public String toString() {
        return title;
    }
}