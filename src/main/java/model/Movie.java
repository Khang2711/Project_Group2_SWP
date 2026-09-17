package model;

import java.sql.Date;
import java.sql.Timestamp;

/** Model cho bảng Movie. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class Movie {
    private int movieId; // movie_id
    private String title; // title
    private String description; // description
    private int durationMinutes; // duration_minutes
    private String ageRating; // age_rating
    private Date releaseDate; // release_date
    private Date endDate; // end_date
    private String posterUrl; // poster_url
    private String status; // status
    private Timestamp createdAt; // created_at
    private Timestamp updatedAt; // updated_at

    public Movie() {
    }

    public Movie(int movieId, String title, String description, int durationMinutes, String ageRating, Date releaseDate, Date endDate, String posterUrl, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.releaseDate = releaseDate;
        this.endDate = endDate;
        this.posterUrl = posterUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
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
}
