
package service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Cinema;
import model.Movie;
import model.Room;
import model.Showtime;
import repository.CinemaRepository;
import repository.MovieRepository;
import repository.RoomRepository;
import repository.ShowtimeRepository;

public class ShowtimeService {

    
    private ShowtimeRepository showtimeRepository = new ShowtimeRepository();
    private RoomRepository roomRepository = new RoomRepository();
    private CinemaRepository cinemaRepository = new CinemaRepository();
    private MovieRepository movieRepository = new MovieRepository();

    /** Lấy lịch chiếu theo phim và ngày. */
    public List<Showtime> getByMovieAndDate(int movieId, LocalDate date) {

        return showtimeRepository.getByMovieIdAndDate(movieId,date);

    }

    /** Lấy các rạp đang hoạt động. */
    public List<Cinema> getActiveCinema() {

        return cinemaRepository.getAllActive();
    }

    /** Tạo bảng tra cứu phòng đang hoạt động. */
    public Map<Integer, Room> getActiveRoomById() {

        Map<Integer, Room> rooms = new HashMap<>();

        for (Room room : roomRepository.getAllActive()) {

            rooms.put(room.getRoomId(),room);
        }

        return rooms;
    }

    /** Hàm này xác định phòng của suất chiếu thuộc rạp nào rồi nhóm các suất chiếu theo ID rạp. */
    public Map<Integer, List<Showtime>> groupByCinema(
            List<Showtime> showtimes,
            Map<Integer, Room> roomsById,
            List<Cinema> cinemas) {

        Map<Integer, List<Showtime>> result =
                new LinkedHashMap<>();

        for (Cinema cinema : cinemas) {

            for (Showtime showtime : showtimes) {

                Room room =
                        roomsById.get(showtime.getRoomId());

                if (room != null
                        && room.getCinemaId()
                        == cinema.getCinemaId()) {
                    //computeIfAbsent() nghĩa là nếu rạp chưa có danh sách suất chiếu thì tạo danh sách mới, sau đó thêm suất chiếu vào.
                    result.computeIfAbsent(
                            cinema.getCinemaId(),
                            key -> new ArrayList<>()
                    ).add(showtime);
                }
            }
        }

        return result;
    }

    // =====================================================
    // KIỂM TRA SUẤT CHIẾU CÓ THỂ ĐẶT VÉ KHÔNG
    // =====================================================

    /** Kiểm tra suất chiếu có thể đặt vé. */
    public boolean isBookable(int showtimeId) {
        //Nếu không tìm thấy suất chiếu thì không cho đặt.
        Showtime showtime =
                showtimeRepository.getById(showtimeId);

        if (showtime == null) {
            return false;
        }
        //Suất chiếu phải ở trạng thái ACTIVE.
        if (!"ACTIVE".equalsIgnoreCase(
                showtime.getStatus())) {
            return false;
        }
        //Lấy thời gian hiện tại theo múi giờ Việt Nam.
        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh"));
        //Nếu suất chiếu đã bắt đầu hoặc không có giờ bắt đầu thì không cho đặt.
        //Code tiếp tục xác nhận ngày chiếu không trước ngày phát hành và không sau ngày kết thúc chiếu của phim.
        if (showtime.getStartTime() == null
                || !showtime.getStartTime()
                        .toLocalDateTime()
                        .isAfter(now)) {

            return false;
        }

        Movie movie = movieRepository.getById(
                showtime.getMovieId()
        );

        if (movie == null) {
            return false;
        }

        LocalDate showDate = showtime.getStartTime()
                .toLocalDateTime()
                .toLocalDate();

        if (movie.getReleaseDate() == null
                || movie.getReleaseDate()
                        .toLocalDate()
                        .isAfter(showDate)) {

            return false;
        }

        if (movie.getEndDate() != null
                && movie.getEndDate()
                        .toLocalDate()
                        .isBefore(showDate)) {

            return false;
        }

        Room room = null;

        for (Room currentRoom :
                roomRepository.getAllActive()) {

            if (currentRoom.getRoomId()
                    == showtime.getRoomId()) {

                room = currentRoom;
                break;
            }
        }

        if (room == null) {
            return false;
        }

        Cinema cinema =
                cinemaRepository.findById(
                        room.getCinemaId()
                );
        //Ngoài suất chiếu, phòng và rạp cũng phải đang hoạt động.
        return cinema != null
                && "ACTIVE".equalsIgnoreCase(
                        cinema.getStatus()
                );
    }

    // =====================================================
    // DANH SÁCH SUẤT CHIẾU CHO MANAGER
    // =====================================================

    /** Lấy lịch chiếu của rạp quản lý. */
    public List<Showtime> getShowtimesForManager(
            int cinemaId) {

        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }

        return showtimeRepository.getByCinemaId(
                cinemaId
        );
    }

    /** Lấy lịch chiếu theo mã. */
    public Showtime getShowtimeById(int showtimeId) {

        if (showtimeId <= 0) {
            return null;
        }

        return showtimeRepository.getById(showtimeId);
    }

    /** Lấy lịch chiếu thuộc đúng rạp quản lý. */
    public Showtime getShowtimeForManager(
            int showtimeId,
            int cinemaId) {

        if (showtimeId <= 0 || cinemaId <= 0) {
            return null;
        }

        return showtimeRepository.getByIdAndCinema(
                showtimeId,
                cinemaId
        );
    }

    // =====================================================
    // TẠO 1 SUẤT CHIẾU
    // =====================================================

    /** Kiểm tra và tạo một suất chiếu. 
     *  1.Nhận ID rạp, phim, phòng, giờ bắt đầu và trạng thái từ Controller.
     *  2.Gọi buildShowtime() để kiểm tra nghiệp vụ và tính giờ kết thúc.
     *  3.Nếu hợp lệ, gọi showtimeRepository.insert() để lưu.
     *  4.Nếu lưu thất bại, báo lỗi. 
     */
    public void createShowtime(
            int cinemaId,
            int movieId,
            int roomId,
            LocalDateTime startTime,
            String status) {

        Showtime showtime = buildShowtime(
                0,
                cinemaId,
                movieId,
                roomId,
                startTime,
                status,
                null
        );

        if (!showtimeRepository.insert(showtime)) {
            throw new IllegalArgumentException(
                    "Không thể thêm lịch chiếu."
            );
        }
    }

    // =====================================================
    // TẠO NHIỀU SUẤT CHIẾU
    // =====================================================

    /** Kiểm tra và tạo nhiều suất chiếu cùng giao dịch. */
    public void createShowtimesBulk(
            int cinemaId,
            int movieId,
            List<Integer> roomIds,
            List<LocalDateTime> startTimes,
            String status) {
        //Không cho truyền danh sách rỗng.
        if (roomIds == null
                || startTimes == null
                || roomIds.isEmpty()
                || startTimes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng thêm ít nhất một ca chiếu."
            );
        }
        //Số phòng và số thời gian phải bằng nhau để mỗi ca chiếu có đủ dữ liệu.
        if (roomIds.size() != startTimes.size()) {
            throw new IllegalArgumentException(
                    "Dữ liệu các ca chiếu không hợp lệ."
            );
        }
        //Giới hạn 50 suất chiếu mỗi lần.
        if (roomIds.size() > 50) {
            throw new IllegalArgumentException(
                    "Mỗi lần chỉ được tạo tối đa 50 ca chiếu."
            );
        }
        //Tạo một danh sách các đối tượng Showtime tên là newShowtimes, khởi tạo bằng một ArrayList rỗng.
        List<Showtime> newShowtimes =
                new ArrayList<>();
        //Mỗi vòng lặp lấy một phòng, một thời gian và gọi buildShowtime() để kiểm tra.
        for (int i = 0; i < roomIds.size(); i++) {

            try {

                if (roomIds.get(i) == null) {
                    throw new IllegalArgumentException(
                            "Phòng chiếu không hợp lệ."
                    );
                }

                Showtime showtime = buildShowtime(
                        0,
                        cinemaId,
                        movieId,
                        roomIds.get(i),
                        startTimes.get(i),
                        status,
                        null
                );

                newShowtimes.add(showtime);

            } catch (IllegalArgumentException e) {
                //Thông báo cho biết ca nào bị lỗi.
                throw new IllegalArgumentException(
                        "Ca " + (i + 1) + ": "
                        + e.getMessage()
                );
            }
        }

        if ("ACTIVE".equals(status)) {
            //Hai vòng for so sánh từng cặp suất chiếu mới.
            for (int i = 0;
                    i < newShowtimes.size();
                    i++) {

                Showtime first = newShowtimes.get(i);

                for (int j = i + 1;
                        j < newShowtimes.size();
                        j++) {

                    Showtime second =
                            newShowtimes.get(j);
                    //Nếu khác phòng thì bỏ qua, vì hai suất chiếu khác phòng có thể diễn ra cùng giờ.
                    if (first.getRoomId()
                            != second.getRoomId()) {
                        continue;
                    }
                    //Kiểm tra thời gian giao nhau.
                    boolean overlap =
                            first.getStartTime().before(
                                    second.getEndTime()
                            )
                            && first.getEndTime().after(
                                    second.getStartTime()
                            );
                    //Nếu trùng không cho tạo danh sách có hai ca ACTIVE bị trùng giờ cùng phòng.
                    if (overlap) {
                        throw new IllegalArgumentException(
                                "Ca " + (i + 1)
                                + " và ca " + (j + 1)
                                + " bị trùng thời gian "
                                + "trong cùng phòng."
                        );
                    }
                }
            }
        }
        //Gọi Repository lưu nhiều suất chiếu.
        if (!showtimeRepository.insertBatch(
                newShowtimes)) {

            throw new IllegalArgumentException(
                    "Không thể tạo loạt lịch chiếu."
            );
        }
    }

    // =====================================================
    // KHÔNG THAY ĐỔI SUẤT CHIẾU ĐÃ CÓ VÉ
    // =====================================================

    /** Sửa suất chiếu nhưng bảo vệ vé đã bán. */
    public void updateShowtime(
            int showtimeId,
            int cinemaId,
            int movieId,
            int roomId,
            LocalDateTime startTime,
            String status) {
        //Tìm lịch chiếu thuộc rạp
        Showtime oldShowtime =
                showtimeRepository.getByIdAndCinema(
                        showtimeId,
                        cinemaId
                );

        if (oldShowtime == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy lịch chiếu."
            );
        }
        //Kiểm tra suất chiếu đã có vé chưa
        if (showtimeRepository.hasTickets(showtimeId)) {
            //Kiểm tra phim và phòng có thay đổi không.
            boolean movieChanged =
                    oldShowtime.getMovieId() != movieId;

            boolean roomChanged =
                    oldShowtime.getRoomId() != roomId;
            //Kiểm tra thời gian bắt đầu có thay đổi không.
            boolean timeChanged =
                    startTime == null
                    || oldShowtime.getStartTime() == null
                    || !oldShowtime.getStartTime()
                            .toLocalDateTime()
                            .equals(startTime);
            //Kiểm tra trạng thái có thay đổi không.
            boolean statusChanged =
                    status == null
                    || !status.equals(
                            oldShowtime.getStatus()
                    );
            //Nếu có lịch sử vé và ít nhất một thông tin thay đổi thì báo lỗi.
            if (movieChanged
                    || roomChanged
                    || timeChanged
                    || statusChanged) {

                throw new IllegalArgumentException(
                        "Không thể thay đổi phim, phòng, "
                        + "thời gian hoặc trạng thái "
                        + "của suất chiếu đã có vé."
                );
            }
        }
        //Cập nhật khi hợp lệ
        //Truyền showtimeId cuối cùng để loại trừ chính suất đang sửa khi kiểm tra trùng giờ.
        Showtime showtime = buildShowtime(
                showtimeId,
                cinemaId,
                movieId,
                roomId,
                startTime,
                status,
                showtimeId
        );
        //Gọi Repository cập nhật.
        if (!showtimeRepository.update(
                showtime,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể cập nhật lịch chiếu."
            );
        }
    }

    // =====================================================
    // KHÔNG XÓA HOẶC VÔ HIỆU HÓA SUẤT ĐÃ CÓ VÉ
    // =====================================================

    /** Chỉ xóa suất chiếu khi chưa có vé. */
    public void deleteShowtime(
            int showtimeId,
            int cinemaId) {
        //Tìm suất chiếu thuộc đúng rạp.
        Showtime showtime =
                showtimeRepository.getByIdAndCinema(
                        showtimeId,
                        cinemaId
                );

        if (showtime == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy lịch chiếu."
            );
        }

        // Nếu đã phát sinh vé thì không cho xóa.
        if (showtimeRepository.hasTickets(showtimeId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa hoặc ngừng hoạt động "
                    + "suất chiếu đã có vé. "
                    + "Vui lòng xử lý vé liên quan "
                    + "theo quy trình trước khi xóa."
            );
        }
        //Nếu chưa có vé thì gọi Repository xóa.
        if (!showtimeRepository.delete(
                showtimeId,
                cinemaId)) {

            throw new IllegalArgumentException(
                    "Không thể xóa lịch chiếu."
            );
        }
    }

    // =====================================================
    // TAO VA KIEM TRA DOI TUONG SHOWTIME
    // KHONG TAO SUAT ACTIVE TAI RAP INACTIVE
    // =====================================================

    /** Kiểm tra phim, phòng, giờ; tính giờ kết thúc. */
    private Showtime buildShowtime(
            int showtimeId,
            int cinemaId,
            int movieId,
            int roomId,
            LocalDateTime startTime,
            String status,
            Integer excludeShowtimeId) {
        //Kiểm tra mã rạp.
        if (cinemaId <= 0) {
            throw new IllegalArgumentException(
                    "Rạp không hợp lệ."
            );
        }
        //Lấy thông tin rạp.
        Cinema cinema =
                cinemaRepository.findById(cinemaId);

        if (cinema == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy rạp chiếu."
            );
        }
        //Kiểm tra phim thuộc rạp
        //Manager chỉ được tạo suất chiếu cho phim thuộc rạp của mình.
        Movie movie =
                movieRepository.getByIdAnyStatus(movieId);

        if (movie == null
                || !movieRepository.belongsToCinema(
                        movieId,
                        cinemaId)) {

            throw new IllegalArgumentException(
                    "Phim không thuộc rạp của bạn."
            );
        }
        //Phòng cũng phải thuộc đúng rạp.
        Room room =
                roomRepository.findByIdAndCinema(
                        roomId,
                        cinemaId
                );

        if (room == null) {
            throw new IllegalArgumentException(
                    "Phòng chiếu không thuộc rạp của bạn."
            );
        }

        if (startTime == null) {
            throw new IllegalArgumentException(
                    "Thời gian bắt đầu không được để trống."
            );
        }
        //Chỉ chấp nhận ACTIVE hoặc INACTIVE.
        //Nếu muốn tạo suất chiếu ACTIVE thì còn yêu cầu:
        //-Rạp ACTIVE.
        //-Phim ACTIVE.
        //-Phòng ACTIVE.
        //-Giờ bắt đầu ở tương lai.
        
        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái lịch chiếu không hợp lệ."
            );
        }

        if ("ACTIVE".equals(status)) {

            if (!"ACTIVE".equalsIgnoreCase(
                    cinema.getStatus())) {

                throw new IllegalArgumentException(
                    "Không thể tạo suất chiếu "
                    + "vì rạp đang ngừng hoạt động."
                );
            }

            if (!"ACTIVE".equalsIgnoreCase(
                    movie.getStatus())) {

                throw new IllegalArgumentException(
                    "Phim đang không hoạt động."
                );
            }

            if (!"ACTIVE".equalsIgnoreCase(
                    room.getStatus())) {

                throw new IllegalArgumentException(
                    "Phòng chiếu đang không hoạt động."
                );
            }

            if (!startTime.isAfter(
                    LocalDateTime.now())) {

                throw new IllegalArgumentException(
                    "Suất chiếu phải bắt đầu trong tương lai."
                );
            }
        }

        LocalDate showDate = startTime.toLocalDate();
        //Kiểm tra ngày phát hành phim
        //Không cho đặt ngày chiếu trước ngày phát hành.
        if (movie.getReleaseDate() != null
                && showDate.isBefore(
                        movie.getReleaseDate()
                                .toLocalDate())) {
            
            throw new IllegalArgumentException(
                    "Ngày chiếu không được trước "
                    + "ngày phát hành phim."
            );
        }
        //Nếu phim đã hết thời gian chiếu theo movie.getEndDate(), hệ thống cũng báo lỗi.
        if (movie.getEndDate() != null
                && showDate.isAfter(
                        movie.getEndDate()
                                .toLocalDate())) {
            
            throw new IllegalArgumentException(
                    "Phim đã hết thời gian chiếu tại rạp."
            );
        }

        if (movie.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "Thời lượng phim không hợp lệ."
            );
        }
        //startTime: Thời gian bắt đầu.
        //movie.getDurationMinutes(): Thời lượng phim, tính bằng phút.
        //plusMinutes(): Cộng thời lượng phim vào giờ bắt đầu.
        LocalDateTime endTime =
                startTime.plusMinutes(
                        movie.getDurationMinutes()
                );
        //Chuyển thời gian sang Timestamp để đưa vào đối tượng Showtime và làm việc với SQL.
        Timestamp startTimestamp =
                Timestamp.valueOf(startTime);

        Timestamp endTimestamp =
                Timestamp.valueOf(endTime);
        //Chặn trùng suất chiếu cùng phòng.
        //Nó kiểm tra:
        //-Suất chiếu muốn tạo/sửa có ACTIVE không.
        //-Trong phòng đó đã có lịch trùng khoảng thời gian chưa.
        //-Nếu đang sửa, loại trừ chính lịch chiếu đang chỉnh sửa.
        if ("ACTIVE".equals(status)
                && showtimeRepository.hasConflict(
                        roomId,
                        startTimestamp,
                        endTimestamp,
                        excludeShowtimeId)) {

            throw new IllegalArgumentException(
                    "Phòng chiếu đã có lịch khác "
                    + "trong khoảng thời gian này."
            );
        }
        //Sau khi vượt qua các kiểm tra, Service tạo đối tượng Showtime để chuyển cho Repository lưu vào database.
        Showtime showtime = new Showtime();

        showtime.setShowtimeId(showtimeId);
        showtime.setMovieId(movieId);
        showtime.setRoomId(roomId);
        showtime.setStartTime(startTimestamp);
        showtime.setEndTime(endTimestamp);
        showtime.setStatus(status);

        return showtime;
    }
}
