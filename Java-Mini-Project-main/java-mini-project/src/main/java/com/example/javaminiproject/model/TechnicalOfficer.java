package com.example.javaminiproject.model;

// INHERITANCE: TechnicalOfficer extends User
public class TechnicalOfficer extends User {

    public TechnicalOfficer(int userId, String username, String password,
                            String fullName, String email, String phone, String department) {
        super(userId, username, password, fullName, email, phone, "TECH_OFFICER", department);
    }

    @Override
    public String getDashboardTitle() { return "Technical Officer Dashboard"; }

    @Override
    public boolean canEditProfile() { return true; }

    public boolean canEditUsername() { return false; }
    public boolean canEditPassword() { return false; }
}