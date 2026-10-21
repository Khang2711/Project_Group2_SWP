package repository;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Pricing;

public class PricingRepository extends DBContext {

    // Giữ lại để MovieService hiện tại không bị vỡ.
    public Map<Integer, BigDecimal> getMinPriceByCinema() {

        Map<Integer, BigDecimal> prices =
                new HashMap<>();

        String sql =
                "SELECT cinema_id, "
                + "MIN(price) AS min_price "
                + "FROM Pricing "
                + "WHERE status = 'ACTIVE' "
                + "AND start_date <= "
                + "CAST(SYSDATETIME() AS DATE) "
                + "AND (end_date IS NULL "
                + "OR end_date >= "
                + "CAST(SYSDATETIME() AS DATE)) "
                + "GROUP BY cinema_id";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                prices.put(
                        rs.getInt("cinema_id"),
                        rs.getBigDecimal("min_price")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return prices;
    }

    public List<Pricing> findByCinemaId(
            int cinemaId) {

        List<Pricing> pricingList =
                new ArrayList<>();

        String sql =
                "SELECT p.pricing_id, "
                + "p.cinema_id, "
                + "p.movie_id, "
                + "m.title AS movie_title, "
                + "p.room_type, "
                + "p.seat_type, "
                + "p.day_type, "
                + "p.special_date, "
                + "p.start_time, "
                + "p.end_time, "
                + "p.price, "
                + "p.start_date, "
                + "p.end_date, "
                + "p.status "
                + "FROM Pricing p "
                + "LEFT JOIN Movie m "
                + "ON p.movie_id = m.movie_id "
                + "WHERE p.cinema_id = ? "
                + "ORDER BY "
                + "p.status DESC, "
                + "p.special_date DESC, "
                + "p.seat_type, "
                + "p.pricing_id DESC";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, cinemaId);

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

    public Pricing findByIdAndCinema(
            int pricingId,
            int cinemaId) {

        String sql =
                "SELECT p.pricing_id, "
                + "p.cinema_id, "
                + "p.movie_id, "
                + "m.title AS movie_title, "
                + "p.room_type, "
                + "p.seat_type, "
                + "p.day_type, "
                + "p.special_date, "
                + "p.start_time, "
                + "p.end_time, "
                + "p.price, "
                + "p.start_date, "
                + "p.end_date, "
                + "p.status "
                + "FROM Pricing p "
                + "LEFT JOIN Movie m "
                + "ON p.movie_id = m.movie_id "
                + "WHERE p.pricing_id = ? "
                + "AND p.cinema_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, pricingId);
            ps.setInt(2, cinemaId);

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

    public boolean insert(
            Pricing pricing) {

        String sql =
                "INSERT INTO Pricing "
                + "(cinema_id, movie_id, room_type, "
                + "seat_type, day_type, special_date, "
                + "start_time, end_time, "
                + "price, start_date, end_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(
                    1,
                    pricing.getCinemaId()
            );

            setNullableInteger(
                    ps,
                    2,
                    pricing.getMovieId()
            );

            setNullableString(
                    ps,
                    3,
                    pricing.getRoomType()
            );

            ps.setString(
                    4,
                    pricing.getSeatType()
            );

            ps.setString(
                    5,
                    pricing.getDayType()
            );

            if (pricing.getSpecialDate() == null) {

                ps.setNull(
                        6,
                        Types.DATE
                );

            } else {

                ps.setDate(
                        6,
                        pricing.getSpecialDate()
                );
            }

            if (pricing.getStartTime() == null) {

                ps.setNull(
                        7,
                        Types.TIME
                );

            } else {

                ps.setTime(
                        7,
                        pricing.getStartTime()
                );
            }

            if (pricing.getEndTime() == null) {

                ps.setNull(
                        8,
                        Types.TIME
                );

            } else {

                ps.setTime(
                        8,
                        pricing.getEndTime()
                );
            }

            ps.setBigDecimal(
                    9,
                    pricing.getPrice()
            );

            ps.setDate(
                    10,
                    pricing.getStartDate()
            );

            if (pricing.getEndDate() == null) {

                ps.setNull(
                        11,
                        Types.DATE
                );

            } else {

                ps.setDate(
                        11,
                        pricing.getEndDate()
                );
            }

            ps.setString(
                    12,
                    pricing.getStatus()
            );

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(
            Pricing pricing) {

        String sql =
                "UPDATE Pricing "
                + "SET movie_id = ?, "
                + "room_type = ?, "
                + "seat_type = ?, "
                + "day_type = ?, "
                + "special_date = ?, "
                + "start_time = ?, "
                + "end_time = ?, "
                + "price = ?, "
                + "start_date = ?, "
                + "end_date = ?, "
                + "status = ? "
                + "WHERE pricing_id = ? "
                + "AND cinema_id = ?";

        try {

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            setNullableInteger(
                    ps,
                    1,
                    pricing.getMovieId()
            );

            setNullableString(
                    ps,
                    2,
                    pricing.getRoomType()
            );

            ps.setString(
                    3,
                    pricing.getSeatType()
            );

            ps.setString(
                    4,
                    pricing.getDayType()
            );

            if (pricing.getSpecialDate() == null) {

                ps.setNull(
                        5,
                        Types.DATE
                );

            } else {

                ps.setDate(
                        5,
                        pricing.getSpecialDate()
                );
            }

            if (pricing.getStartTime() == null) {

                ps.setNull(
                        6,
                        Types.TIME
                );

            } else {

                ps.setTime(
                        6,
                        pricing.getStartTime()
                );
            }

            if (pricing.getEndTime() == null) {

                ps.setNull(
                        7,
                        Types.TIME
                );

            } else {

                ps.setTime(
                        7,
                        pricing.getEndTime()
                );
            }

            ps.setBigDecimal(
                    8,
                    pricing.getPrice()
            );

            ps.setDate(
                    9,
                    pricing.getStartDate()
            );

            if (pricing.getEndDate() == null) {

                ps.setNull(
                        10,
                        Types.DATE
                );

            } else {

                ps.setDate(
                        10,
                        pricing.getEndDate()
                );
            }

            ps.setString(
                    11,
                    pricing.getStatus()
            );

            ps.setInt(
                    12,
                    pricing.getPricingId()
            );

            ps.setInt(
                    13,
                    pricing.getCinemaId()
            );

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

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

            ps.setInt(1, pricingId);
            ps.setInt(2, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setNullableInteger(
            PreparedStatement ps,
            int index,
            Integer value)
            throws Exception {

        if (value == null) {

            ps.setNull(
                    index,
                    Types.INTEGER
            );

        } else {

            ps.setInt(
                    index,
                    value
            );
        }
    }

    private void setNullableString(
            PreparedStatement ps,
            int index,
            String value)
            throws Exception {

        if (value == null) {

            ps.setNull(
                    index,
                    Types.VARCHAR
            );

        } else {

            ps.setString(
                    index,
                    value
            );
        }
    }

    private Pricing mapPricing(
            ResultSet rs)
            throws Exception {

        Integer movieId = null;

        int movieValue =
                rs.getInt("movie_id");

        if (!rs.wasNull()) {
            movieId = movieValue;
        }

        return new Pricing(
                rs.getInt("pricing_id"),
                rs.getInt("cinema_id"),
                movieId,
                rs.getString("movie_title"),
                rs.getString("room_type"),
                rs.getString("seat_type"),
                rs.getString("day_type"),
                rs.getDate("special_date"),
                rs.getTime("start_time"),
                rs.getTime("end_time"),
                rs.getBigDecimal("price"),
                rs.getDate("start_date"),
                rs.getDate("end_date"),
                rs.getString("status")
        );
    }
}