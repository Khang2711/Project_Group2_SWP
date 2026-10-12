package repository;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Pricing;

public class PricingRepository extends DBContext {

    // =========================================================
    // METHOD CŨ
    // Giữ lại vì MovieService đang sử dụng.
    // Lấy giá thấp nhất đang có hiệu lực của từng rạp.
    // =========================================================
    public Map<Integer, BigDecimal> getMinPriceByCinema() {

        Map<Integer, BigDecimal> prices =
                new HashMap<>();

        String sql =
                "SELECT cinema_id, "
                + "MIN(price) AS min_price "
                + "FROM Pricing "
                + "WHERE status = ? "
                + "AND start_date <= "
                + "CAST(SYSDATETIME() AS DATE) "
                + "AND (end_date IS NULL "
                + "OR end_date >= "
                + "CAST(SYSDATETIME() AS DATE)) "
                + "GROUP BY cinema_id";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(
                    1,
                    "ACTIVE"
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                int cinemaId =
                        rs.getInt(
                                "cinema_id"
                        );

                BigDecimal minPrice =
                        rs.getBigDecimal(
                                "min_price"
                        );

                prices.put(
                        cinemaId,
                        minPrice
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return prices;
    }

    // =========================================================
    // LẤY TẤT CẢ GIÁ CỦA MỘT RẠP
    // =========================================================
    public List<Pricing> findByCinemaId(
            int cinemaId) {

        List<Pricing> pricingList =
                new ArrayList<>();

        String sql =
                "SELECT pricing_id, cinema_id, "
                + "seat_type, price, "
                + "start_date, end_date, status "
                + "FROM Pricing "
                + "WHERE cinema_id = ? "
                + "ORDER BY seat_type, "
                + "start_date DESC, pricing_id DESC";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    cinemaId
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                pricingList.add(
                        mapPricing(rs)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return pricingList;
    }

    // =========================================================
    // TÌM 1 PRICING
    // Đồng thời kiểm tra thuộc đúng rạp của Manager.
    // =========================================================
    public Pricing findByIdAndCinema(
            int pricingId,
            int cinemaId) {

        String sql =
                "SELECT pricing_id, cinema_id, "
                + "seat_type, price, "
                + "start_date, end_date, status "
                + "FROM Pricing "
                + "WHERE pricing_id = ? "
                + "AND cinema_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    pricingId
            );

            ps.setInt(
                    2,
                    cinemaId
            );

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                return mapPricing(rs);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // =========================================================
    // KIỂM TRA KHOẢNG GIÁ ACTIVE BỊ CHỒNG
    //
    // Ví dụ:
    // giá cũ: 01/10 -> 31/10
    // giá mới: 15/10 -> 15/11
    // => overlap = true
    // =========================================================
    public boolean hasOverlappingActivePeriod(
            int cinemaId,
            String seatType,
            Date startDate,
            Date endDate,
            Integer excludePricingId) {

        StringBuilder sql =
                new StringBuilder();

        sql.append(
                "SELECT TOP 1 pricing_id "
        );

        sql.append(
                "FROM Pricing "
        );

        sql.append(
                "WHERE cinema_id = ? "
        );

        sql.append(
                "AND seat_type = ? "
        );

        sql.append(
                "AND status = 'ACTIVE' "
        );

        if (excludePricingId != null) {

            sql.append(
                    "AND pricing_id <> ? "
            );
        }

        // Pricing cũ phải chưa kết thúc
        // trước ngày pricing mới bắt đầu.
        sql.append(
                "AND (end_date IS NULL "
                + "OR end_date >= ?) "
        );

        // Nếu pricing mới có end date
        // thì pricing cũ phải bắt đầu
        // trước hoặc đúng end date mới.
        if (endDate != null) {

            sql.append(
                    "AND start_date <= ? "
            );
        }

        try {

            PreparedStatement ps =
                    conn.prepareStatement(
                            sql.toString()
                    );

            int index = 1;

            ps.setInt(
                    index++,
                    cinemaId
            );

            ps.setString(
                    index++,
                    seatType
            );

            if (excludePricingId != null) {

                ps.setInt(
                        index++,
                        excludePricingId
                );
            }

            ps.setDate(
                    index++,
                    startDate
            );

            if (endDate != null) {

                ps.setDate(
                        index,
                        endDate
                );
            }

            ResultSet rs =
                    ps.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // THÊM GIÁ
    // =========================================================
    public boolean insert(
            Pricing pricing) {

        String sql =
                "INSERT INTO Pricing "
                + "(cinema_id, seat_type, price, "
                + "start_date, end_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    pricing.getCinemaId()
            );

            ps.setString(
                    2,
                    pricing.getSeatType()
            );

            ps.setBigDecimal(
                    3,
                    pricing.getPrice()
            );

            ps.setDate(
                    4,
                    pricing.getStartDate()
            );

            if (pricing.getEndDate()
                    == null) {

                ps.setNull(
                        5,
                        java.sql.Types.DATE
                );

            } else {

                ps.setDate(
                        5,
                        pricing.getEndDate()
                );
            }

            ps.setString(
                    6,
                    pricing.getStatus()
            );

            return ps.executeUpdate()
                    > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // CẬP NHẬT GIÁ
    // =========================================================
    public boolean update(
            Pricing pricing) {

        String sql =
                "UPDATE Pricing "
                + "SET seat_type = ?, "
                + "price = ?, "
                + "start_date = ?, "
                + "end_date = ?, "
                + "status = ? "
                + "WHERE pricing_id = ? "
                + "AND cinema_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(
                    1,
                    pricing.getSeatType()
            );

            ps.setBigDecimal(
                    2,
                    pricing.getPrice()
            );

            ps.setDate(
                    3,
                    pricing.getStartDate()
            );

            if (pricing.getEndDate()
                    == null) {

                ps.setNull(
                        4,
                        java.sql.Types.DATE
                );

            } else {

                ps.setDate(
                        4,
                        pricing.getEndDate()
                );
            }

            ps.setString(
                    5,
                    pricing.getStatus()
            );

            ps.setInt(
                    6,
                    pricing.getPricingId()
            );

            ps.setInt(
                    7,
                    pricing.getCinemaId()
            );

            return ps.executeUpdate()
                    > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // XÓA GIÁ
    // =========================================================
    public boolean delete(
            int pricingId,
            int cinemaId) {

        String sql =
                "DELETE FROM Pricing "
                + "WHERE pricing_id = ? "
                + "AND cinema_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    pricingId
            );

            ps.setInt(
                    2,
                    cinemaId
            );

            return ps.executeUpdate()
                    > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // =========================================================
    // RESULTSET -> PRICING
    // =========================================================
    private Pricing mapPricing(
            ResultSet rs)
            throws Exception {

        return new Pricing(
                rs.getInt("pricing_id"),
                rs.getInt("cinema_id"),
                rs.getString("seat_type"),
                rs.getBigDecimal("price"),
                rs.getDate("start_date"),
                rs.getDate("end_date"),
                rs.getString("status")
        );
    }
}