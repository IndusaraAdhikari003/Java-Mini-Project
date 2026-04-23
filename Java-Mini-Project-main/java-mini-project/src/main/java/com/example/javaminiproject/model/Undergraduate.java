package com.example.javaminiproject.model;

// INHERITANCE: Undergraduate extends User
public class Undergraduate extends User {

    private int     ugId;
    private String  regNumber;
    private String  batch;
    private boolean isRepeat;
    private boolean batchMissed;

    //--accepts department from DB
    public Undergraduate(int userId, String username, String password,
                         String fullName, String email, String phone,
                         String department,
                         int ugId, String regNumber, String batch,
                         boolean isRepeat, boolean batchMissed) {
        super(userId, username, password, fullName, email, phone, "UNDERGRADUATE", department);
        this.ugId        = ugId;
        this.regNumber   = regNumber;
        this.batch       = batch;
        this.isRepeat    = isRepeat;
        this.batchMissed = batchMissed;
    }

    public int     getUgId()       { return ugId; }
    public String  getRegNumber()  { return regNumber; }
    public String  getBatch()      { return batch; }
    public boolean isRepeat()      { return isRepeat; }
    public boolean isBatchMissed() { return batchMissed; }

    // POLYMORPHISM: UG can only edit contact + picture, not full profile
    @Override
    public String getDashboardTitle() { return "Student Portal"; }

    @Override
    public boolean canEditProfile() { return false; } // limited edit only
}