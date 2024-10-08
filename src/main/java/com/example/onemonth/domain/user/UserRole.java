package com.example.onemonth.domain.user;

public enum UserRole {
    ADMIN("ADMIN"),
    USER("USER");

    private String authority;
    UserRole(String authority) {
        this.authority = authority;
    }
    public String getAuthority() {
        return this.authority;
    }
}
