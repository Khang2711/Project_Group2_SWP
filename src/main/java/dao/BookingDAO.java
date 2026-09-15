package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.CartItem;
import model.ComboItem;
import model.MovieRevenue;
import model.Ticket;
import utils.DBUtils;

public class BookingDAO {

    private List<ComboItem> getDefaultCombos() {
        List<ComboItem> list = new ArrayList<>();
        list.add(new ComboItem("combo-popcorn", "Combo Solo: 1 bắp vừa + 1 nước vừa", 45000, 1));
        list.add(new ComboItem("combo-couple", "Combo Couple: 1 bắp lớn + 2 nước vừa", 89000, 1));
        list.add(new ComboItem("combo-family", "Combo Family: 2 bắp lớn + 4 nước vừa", 149000, 1));
        return list;
    }

    public List<ComboItem> getAllCombos() throws Exception {
        return getDefaultCombos();
    }

    public ComboItem getComboByID(String comboID) throws Exception {
        if (comboID == null) {
            return null;
        }
        for (ComboItem combo : getDefaultCombos()) {
            if (combo.getComboID().equals(comboID)) {
                return combo;
            }
        }
        return null;
    }

    private void ensureComboExists(Connection conn, ComboItem combo) throws Exception {
        String sql = "IF NOT EXISTS (SELECT 1 FROM tblCombos WHERE comboID = ?) "
                + "INSERT INTO tblCombos(comboID, comboName, price, status) VALUES(?, ?, ?, 1) "
                + "ELSE UPDATE tblCombos SET comboName = ?, price = ?, status = 1 WHERE comboID = ?";

        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, combo.getComboID());
            stm.setString(2, combo.getComboID());
            stm.setString(3, combo.getComboName());
            stm.setFloat(4, combo.getPrice());
            stm.setString(5, combo.getComboName());
            stm.setFloat(6, combo.getPrice());
            stm.setString(7, combo.getComboID());
            stm.executeUpdate();
        }
    }

    public List<String> getBookedSeats(String showtimeID) throws Exception {
        List<String> list = new ArrayList<>();
        String sql = "SELECT seatID FROM tblTickets WHERE showtimeID = ? AND status = 1 ORDER BY seatID";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, showtimeID);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("seatID"));
                }
            }
        }
        return list;
    }

    public boolean isSeatBooked(String showtimeID, String seatID) throws Exception {
        String sql = "SELECT ticketID FROM tblTickets WHERE showtimeID = ? AND seatID = ? AND status = 1";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, showtimeID);
            stm.setString(2, seatID);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean createBooking(String userID, String showtimeID, String[] seats, float price) throws Exception {
        if (seats == null || seats.length == 0) {
            return false;
        }

        List<CartItem> items = new ArrayList<>();
        for (String seat : seats) {
            items.add(new CartItem(showtimeID, "", "", "", "", "", seat, price));
        }
        return createBookingFromCart(userID, items);
    }

    public boolean createBookingFromCart(String userID, List<CartItem> cart) throws Exception {
        return createBookingFromCart(userID, cart, new ArrayList<ComboItem>());
    }

    public boolean createBookingFromCart(String userID, List<CartItem> cart, List<ComboItem> combos) throws Exception {
        throw new UnsupportedOperationException("Dùng BookingService.reserve và thông báo thanh toán có chữ ký để phát hành vé.");
    }

    public List<Ticket> getHistoryByUser(String userID) throws Exception {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT t.ticketID, t.orderID, m.title, r.roomName, "
                + "CONVERT(varchar(10), s.showDate, 120) AS showDate, "
                + "CONVERT(varchar(5), s.startTime, 108) AS startTime, "
                + "t.seatID, t.price, CONVERT(varchar(19), o.orderDate, 120) AS orderDate "
                + "FROM tblTickets t "
                + "JOIN tblOrders o ON t.orderID = o.orderID "
                + "JOIN tblShowtimes s ON t.showtimeID = s.showtimeID "
                + "JOIN tblMovies m ON s.movieID = m.movieID "
                + "JOIN tblRooms r ON s.roomID = r.roomID "
                + "WHERE o.userID = ? AND t.status = 1 "
                + "ORDER BY o.orderDate DESC, t.ticketID";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, userID);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(new Ticket(
                            rs.getString("ticketID"),
                            rs.getString("orderID"),
                            rs.getString("title"),
                            rs.getString("roomName"),
                            rs.getString("showDate"),
                            rs.getString("startTime"),
                            rs.getString("seatID"),
                            rs.getFloat("price"),
                            rs.getString("orderDate")
                    ));
                }
            }
        }
        return list;
    }


    public List<MovieRevenue> getRevenueByMovie() throws Exception {
        List<MovieRevenue> list = new ArrayList<>();
        String sql = "SELECT m.movieID, m.title, "
                + "COUNT(DISTINCT CASE WHEN t.status = 1 AND o.status = 1 THEN t.orderID END) AS orderCount, "
                + "COUNT(CASE WHEN t.status = 1 AND o.status = 1 THEN 1 END) AS ticketCount, "
                + "ISNULL(SUM(CASE WHEN t.status = 1 AND o.status = 1 THEN t.price ELSE 0 END), 0) AS revenue "
                + "FROM tblMovies m "
                + "LEFT JOIN tblShowtimes s ON m.movieID = s.movieID "
                + "LEFT JOIN tblTickets t ON s.showtimeID = t.showtimeID "
                + "LEFT JOIN tblOrders o ON t.orderID = o.orderID "
                + "GROUP BY m.movieID, m.title "
                + "ORDER BY revenue DESC, m.title";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                list.add(new MovieRevenue(
                        rs.getString("movieID"),
                        rs.getString("title"),
                        rs.getInt("orderCount"),
                        rs.getInt("ticketCount"),
                        rs.getFloat("revenue")
                ));
            }
        }
        return list;
    }

    public int countOrders() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM tblOrders WHERE status = 1";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    public int countTickets() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM tblTickets WHERE status = 1";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    public float getRevenue() throws Exception {
        String sql = "SELECT ISNULL(SUM(totalAmount), 0) AS revenue FROM tblOrders WHERE status = 1";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {
            return rs.next() ? rs.getFloat("revenue") : 0;
        }
    }

    private void setPeriod(PreparedStatement stm, LocalDate startDate, LocalDate endDate) throws Exception {
        stm.setDate(1, java.sql.Date.valueOf(startDate));
        stm.setDate(2, java.sql.Date.valueOf(endDate));
    }

    /**
     * Thống kê doanh thu vé của từng phim theo thời gian tạo đơn hàng.
     * startDate được tính, endDate không được tính (ví dụ 01/07 đến trước 01/08).
     */
    public List<MovieRevenue> getRevenueByMovie(LocalDate startDate, LocalDate endDate) throws Exception {
        List<MovieRevenue> list = new ArrayList<>();
        String sql = "SELECT m.movieID, m.title, "
                + "COUNT(DISTINCT t.orderID) AS orderCount, "
                + "COUNT(*) AS ticketCount, "
                + "ISNULL(SUM(t.price), 0) AS revenue "
                + "FROM tblTickets t "
                + "JOIN tblOrders o ON t.orderID = o.orderID "
                + "JOIN tblShowtimes s ON t.showtimeID = s.showtimeID "
                + "JOIN tblMovies m ON s.movieID = m.movieID "
                + "WHERE t.status = 1 AND o.status = 1 "
                + "AND o.orderDate >= ? AND o.orderDate < ? "
                + "GROUP BY m.movieID, m.title "
                + "ORDER BY revenue DESC, m.title";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            setPeriod(stm, startDate, endDate);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(new MovieRevenue(
                            rs.getString("movieID"),
                            rs.getString("title"),
                            rs.getInt("orderCount"),
                            rs.getInt("ticketCount"),
                            rs.getFloat("revenue")
                    ));
                }
            }
        }
        return list;
    }

    public int countOrders(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM tblOrders "
                + "WHERE status = 1 AND orderDate >= ? AND orderDate < ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {
            setPeriod(stm, startDate, endDate);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public int countTickets(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = "SELECT COUNT(*) AS total "
                + "FROM tblTickets t JOIN tblOrders o ON t.orderID = o.orderID "
                + "WHERE t.status = 1 AND o.status = 1 "
                + "AND o.orderDate >= ? AND o.orderDate < ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {
            setPeriod(stm, startDate, endDate);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public int countMoviesWithRevenue(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = "SELECT COUNT(DISTINCT s.movieID) AS total "
                + "FROM tblTickets t "
                + "JOIN tblOrders o ON t.orderID = o.orderID "
                + "JOIN tblShowtimes s ON t.showtimeID = s.showtimeID "
                + "WHERE t.status = 1 AND o.status = 1 "
                + "AND o.orderDate >= ? AND o.orderDate < ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {
            setPeriod(stm, startDate, endDate);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public float getTicketRevenue(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = "SELECT ISNULL(SUM(t.price), 0) AS revenue "
                + "FROM tblTickets t JOIN tblOrders o ON t.orderID = o.orderID "
                + "WHERE t.status = 1 AND o.status = 1 "
                + "AND o.orderDate >= ? AND o.orderDate < ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {
            setPeriod(stm, startDate, endDate);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getFloat("revenue") : 0;
            }
        }
    }

    public float getComboRevenue(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = "SELECT ISNULL(SUM(oc.quantity * oc.price), 0) AS revenue "
                + "FROM tblOrderCombos oc JOIN tblOrders o ON oc.orderID = o.orderID "
                + "WHERE o.status = 1 AND o.orderDate >= ? AND o.orderDate < ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {
            setPeriod(stm, startDate, endDate);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getFloat("revenue") : 0;
            }
        }
    }

    public float getRevenue(LocalDate startDate, LocalDate endDate) throws Exception {
        String sql = "SELECT ISNULL(SUM(totalAmount), 0) AS revenue FROM tblOrders "
                + "WHERE status = 1 AND orderDate >= ? AND orderDate < ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {
            setPeriod(stm, startDate, endDate);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getFloat("revenue") : 0;
            }
        }
    }

}
