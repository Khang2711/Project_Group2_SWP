package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Một card phim trong một ngày chiếu.
 * Card chứa thông tin phim và toàn bộ giờ chiếu còn hiệu lực của ngày đó.
 */
public class MovieSchedule {

    private Movie movie;
    private String showDate;
    private List<Showtime> showtimes;

    public MovieSchedule() {
        this.showtimes = new ArrayList<>();
    }

    public MovieSchedule(Movie movie, String showDate) {
        this.movie = movie;
        this.showDate = showDate;
        this.showtimes = new ArrayList<>();
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    public void addShowtime(Showtime showtime) {
        this.showtimes.add(showtime);
    }
}
