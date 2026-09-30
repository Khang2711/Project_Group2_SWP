package model;

import java.sql.Date;

/**
 * Represents a movie in the cinema system.
 *
 * @author Kiet
 */
public class Movie {

    private int movieId;
    private String title;
    private String description;
    private int durationMinutes;
    private String ageRating;
    private Date releaseDate;
    private Date endDate;
    private String posterUrl;
    private String trailerUrl;
    private String status;

    public Movie() {
    }

    public Movie(int movieId, String title, String description, int durationMinutes, String ageRating, Date releaseDate, Date endDate, String posterUrl, String trailerUrl, String status) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.releaseDate = releaseDate;
        this.endDate = endDate;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.status = status;
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

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
