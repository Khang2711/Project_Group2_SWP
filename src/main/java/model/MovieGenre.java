package model;

/**
 * Represents the relationship between a movie and a genre.
 *
 * @author Kiet
 */
public class MovieGenre {

    private int movieId;
    private int genreId;

    public MovieGenre() {
    }
    
    public MovieGenre(int movieId, int genreId) {
        this.movieId = movieId;
        this.genreId = genreId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }
}