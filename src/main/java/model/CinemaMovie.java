package model;

/**
 * Represents the relationship between a cinema and a movie.
 *
 * @author Kiet
 */
public class CinemaMovie {

    private int cinemaId;
    private int movieId;
    private String status;

    public CinemaMovie() {
    }

    public CinemaMovie(int cinemaId, int movieId, String status) {
        this.cinemaId = cinemaId;
        this.movieId = movieId;
        this.status = status;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}