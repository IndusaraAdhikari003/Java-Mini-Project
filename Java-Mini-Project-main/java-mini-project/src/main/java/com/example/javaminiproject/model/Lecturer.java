package com.example.javaminiproject.model;

// INHERITANCE: Lecturer extends User
public class Lecturer extends User {

    public Lecturer(int userId, String username, String password,
                    String fullName, String email, String phone, String department) {
        super(userId, username, password, fullName, email, phone, "LECTURER", department);
    }

    @Override
    public String getDashboardTitle() { return "Lecturer Dashboard"; }

    // POLYMORPHISM: Lecturer CAN edit profile but NOT username/password
    @Override
    public boolean canEditProfile() { return true; }

    public boolean canEditUsername() { return false; }
    public boolean canEditPassword() { return false; }
}