package com.example.javaminiproject.model;

import java.time.LocalDate;

public class Medical {
    private int       medicalId;
    private int       ugId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String    reason;
    private String    docPath;
    private boolean   isApproved;
    private String    status;
    private String    studentName;

    public Medical(int medicalId, int ugId, LocalDate fromDate, LocalDate toDate,
                   String reason, String docPath, boolean isApproved) {
        this.medicalId  = medicalId;
        this.ugId       = ugId;
        this.fromDate   = fromDate;
        this.toDate     = toDate;
        this.reason     = reason;
        this.docPath    = docPath;
        this.isApproved = isApproved;
        this.status     = isApproved ? "APPROVED" : "PENDING";
    }

    public int       getMedicalId()  { return medicalId; }
    public int       getUgId()       { return ugId; }
    public LocalDate getFromDate()   { return fromDate; }
    public LocalDate getToDate()     { return toDate; }
    public String    getReason()     { return reason; }
    public String    getDocPath()    { return docPath; }
    public boolean   isApproved()    { return isApproved; }
    public String    getStatus()     { return status; }
    public String    getStudentName(){ return studentName; }

    public void setApproved(boolean a)   { this.isApproved = a; }
    public void setStatus(String s)      { this.status = s; }
    public void setStudentName(String s) { this.studentName = s; }

    public boolean isPending()  { return "PENDING".equals(status); }
    public boolean isRejected() { return "REJECTED".equals(status); }
}