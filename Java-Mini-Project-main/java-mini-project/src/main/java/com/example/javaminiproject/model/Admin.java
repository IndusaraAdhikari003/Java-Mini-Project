package com.example.javaminiproject.model;

// INHERITANCE: Admin extends User
public class Admin extends User {

    public Admin(int userId, String username, String password,
                 String fullName, String email, String phone) {
        super(userId, username, password, fullName, email, phone, "ADMIN", "Administration");
    }

    @Override
    public String getDashboardTitle() { return "Admin Dashboard"; }

    @Override
    public boolean canEditProfile() { return true; }
}