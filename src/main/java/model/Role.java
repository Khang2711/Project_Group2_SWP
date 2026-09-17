package model;


/** Model cho bảng Role. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Role {
    private int roleId; // role_id
    private String roleName; // role_name
    private String description; // description
    private String status; // status

    public Role() {
    }

    public Role(int roleId, String roleName, String description, String status) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.status = status;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
