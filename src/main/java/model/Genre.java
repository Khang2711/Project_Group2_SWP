package model;

/**
 * Represents a movie genre.
 *
 * @author Kiet
 */
public class Genre {

    private int genreId;
    private String genreName;
    private String description;
    private String status;

    public Genre() {
    }

    public Genre(int genreId, String genreName, String description, String status) {
        this.genreId = genreId;
        this.genreName = genreName;
        this.description = description;
        this.status = status;
    }
    
    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
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
