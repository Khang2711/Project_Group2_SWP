package model;

import java.sql.Date;
import java.sql.Time;

/** Model cho bảng Showtime. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Showtime {
    private int showtimeId; // showtime_id
    private int roomId; // room_id
    private int movieId; // movie_id
    private Date showDate; // show_date
    private Time startTime; // start_time
    private Time endTime; // end_time
    private String status; // status

    public Showtime() {
    }

    public Showtime(int showtimeId, int roomId, int movieId, Date showDate, Time startTime, Time endTime, String status) {
        this.showtimeId = showtimeId;
        this.roomId = roomId;
        this.movieId = movieId;
        this.showDate = showDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public Date getShowDate() {
        return showDate;
    }

    public void setShowDate(Date showDate) {
        this.showDate = showDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
