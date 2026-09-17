package model;


/** Model cho bảng Cinema_Movie. Chỉ chứa dữ liệu, chưa có xử lý nghiệp vụ. */
public class CinemaMovie {
    private int cinemaId; // cinema_id
    private int movieId; // movie_id
    private String status; // status

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
