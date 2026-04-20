package com.example.javaminiproject.model;

// ABSTRACTION: Abstract class cannot be instantiated directly
// ENCAPSULATION: All fields are private, accessed via getters/setters
public abstract class User {

    private int    userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String profilePic;
    private String role;
    private String department;

    public User(int userId, String username, String password,
                String fullName, String email, String phone,
                String role, String department) {
        this.userId     = userId;
        this.username   = username;
        this.password   = password;
        this.fullName   = fullName;
        this.email      = email;
        this.phone      = phone;
        this.role       = role;
        this.department = department;
    }

    // ABSTRACTION: Every subclass must implement this
    public abstract String getDashboardTitle();
    public abstract boolean canEditProfile();

    // Getters
    public int    getUserId()     { return userId; }
    public String getUsername()   { return username; }
    public String getPassword()   { return password; }
    public String getFullName()   { return fullName; }
    public String getEmail()      { return email; }
    public String getPhone()      { return phone; }
    public String getProfilePic() { return profilePic; }
    public String getRole()       { return role; }
    public String getDepartment() { return department; }

    // Setters — only for updatable fields
    public void setFullName(String fullName)   { this.fullName   = fullName; }
    public void setEmail(String email)         { this.email      = email; }
    public void setPhone(String phone)         { this.phone      = phone; }
    public void setProfilePic(String path)     { this.profilePic = path; }
    public void setDepartment(String dept)     { this.department = dept; }

    @Override
    public String toString() {
        return fullName + " [" + role + "]";
    }
}