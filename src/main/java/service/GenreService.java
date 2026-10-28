package service;

import java.util.List;
import model.Genre;
import repository.GenreRepository;

public class GenreService {

    private GenreRepository genreRepository = new GenreRepository();

    /**
     * Gets all genres, including active and inactive genres.
     *
     * @return a list of all genres
     */
    public List<Genre> getAllGenres() {
        return genreRepository.getAll();
    }

    /**
     * Gets a genre by its ID.
     *
     * @param genreId the genre ID
     * @return the genre if found, otherwise null
     */
    public Genre getGenreById(int genreId) {
        return genreRepository.getById(genreId);
    }

    /**
     * Creates a new genre.
     *
     * @param genre the genre to create
     * @return an error message, or null if successful
     */
    public String createGenre(Genre genre) {

        if (genre == null) {
            return "Thông tin thể loại không hợp lệ.";
        }

        String error = validateGenre(genre);

        if (error != null) {
            return error;
        }

        if (genreRepository.existsByName(genre.getGenreName())) {
            return "Tên thể loại đã tồn tại.";
        }

        if (!genreRepository.insert(genre)) {
            return "Không thể thêm thể loại.";
        }

        return null;
    }

    /**
     * Updates an existing genre.
     *
     * @param genre the genre with updated information
     * @return an error message, or null if successful
     */
    public String updateGenre(Genre genre) {

        if (genre == null || genre.getGenreId() <= 0) {
            return "Thể loại không hợp lệ.";
        }

        Genre oldGenre = genreRepository.getById(genre.getGenreId());

        if (oldGenre == null) {
            return "Không tìm thấy thể loại.";
        }

        String error = validateGenre(genre);

        if (error != null) {
            return error;
        }

        if (!oldGenre.getGenreName().equalsIgnoreCase(genre.getGenreName())) {

            if (genreRepository.existsByName(genre.getGenreName())) {
                return "Tên thể loại đã tồn tại.";
            }
        }

        if (!genreRepository.update(genre)) {
            return "Không thể cập nhật thể loại.";
        }

        return null;
    }

    /**
     * Changes the status of a genre.
     *
     * @param genreId the genre ID
     * @param status the new status (ACTIVE or INACTIVE)
     * @return an error message, or null if successful
     */
    public String updateStatus(int genreId, String status) {

        Genre genre = genreRepository.getById(genreId);

        if (genre == null) {
            return "Không tìm thấy thể loại.";
        }

        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            return "Trạng thái không hợp lệ.";
        }

        if (status.equals(genre.getStatus())) {
            return null;
        }

        if (!genreRepository.updateStatus(genreId, status)) {
            return "Không thể cập nhật trạng thái.";
        }

        return null;
    }

    /**
     * Permanently deletes a genre when it has no linked movies.
     *
     * @return an error message, or null if deleted
     */
    public String deleteGenre(int genreId) {
        if (genreId <= 0) {
            return "Mã thể loại không hợp lệ.";
        }

        switch (genreRepository.deleteIfUnused(genreId)) {
            case DELETED:
                return null;
            case IN_USE:
                return "Không thể xóa thể loại vì đang có phim hoặc dữ liệu liên quan.";
            case NOT_FOUND:
                return "Không tìm thấy thể loại.";
            default:
                return "Không thể xóa thể loại. Vui lòng thử lại.";
        }
    }

    /**
     * Validates genre information before saving.
     *
     * @param genre the genre to validate
     * @return an error message, or null if valid
     */
    private String validateGenre(Genre genre) {

        String genreName = genre.getGenreName();
        String description = genre.getDescription();
        String status = genre.getStatus();

        if (genreName == null || genreName.trim().isEmpty()) {
            return "Tên thể loại không được để trống.";
        }

        genreName = genreName.trim();

        if (genreName.length() > 100) {
            return "Tên thể loại không được vượt quá 100 ký tự.";
        }

        if (description != null) {

            description = description.trim();

            if (description.length() > 255) {
                return "Mô tả không được vượt quá 255 ký tự.";
            }
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            return "Trạng thái không hợp lệ.";
        }

        genre.setGenreName(genreName);
        genre.setDescription(description);

        return null;
    }
}
