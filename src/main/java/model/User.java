package model;

import java.sql.Timestamp;

/** Model cho bảng User. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class User {
    private int userId; // user_id
    private int roleId; // role_id
    private Integer cinemaId; // cinema_id
    private String fullName; // full_name
    private String email; // email
    private String phone; // phone
    private String passwordHash; // password_hash
    private String avatarUrl; // avatar_url
    private String status; // status
    private Timestamp createdAt; // created_at
    private Timestamp updatedAt; // updated_at

    public User() {
    }

    public User(int userId, int roleId, Integer cinemaId, String fullName, String email, String phone, String passwordHash, String avatarUrl, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.userId = userId;
        this.roleId = roleId;
        this.cinemaId = cinemaId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Integer getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    // Tên hiển thị lấy từ JOIN; không phải cột mới trong bảng User.
    private String roleName;
    private String cinemaName;
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
}