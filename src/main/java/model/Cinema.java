package model;


/** Model cho bảng Cinema. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Cinema {
    private int cinemaId; // cinema_id
    private String cinemaName; // cinema_name
    private String address; // address
    private String phone; // phone
    private String status; // status

    public Cinema() {
    }

    public Cinema(int cinemaId, String cinemaName, String address, String phone, String status) {
        this.cinemaId = cinemaId;
        this.cinemaName = cinemaName;
        this.address = address;
        this.phone = phone;
        this.status = status;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
