package repository;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides data access operations for Pricing.
 *
 * @author Kiet
 */
public class PricingRepository extends DBContext {

    /**
     * Gets the lowest ticket price currently in effect of each cinema.
     *
     * @return a map of cinema ID and its lowest active price
     */
    public Map<Integer, BigDecimal> getMinPriceByCinema() {

        Map<Integer, BigDecimal> prices = new HashMap<>();

        String sql = "SELECT cinema_id, MIN(price) AS min_price\n"
                + "FROM Pricing\n"
                + "WHERE status = ?\n"
                + "AND start_date <= CAST(SYSDATETIME() AS DATE)\n"
                + "AND (end_date IS NULL OR end_date >= CAST(SYSDATETIME() AS DATE))\n"
                + "GROUP BY cinema_id";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "ACTIVE");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int cinemaId = rs.getInt("cinema_id");
                BigDecimal minPrice = rs.getBigDecimal("min_price");

                prices.put(cinemaId, minPrice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prices;
    }
}