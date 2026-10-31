package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Promotion;

public class PromotionRepository {

    private String getSelectSql() {
        return "SELECT p.promotion_id, p.cinema_id, p.promo_code, "
                + "p.description, p.discount_type, p.discount_value, "
                + "p.min_order_amount, p.max_discount_amount, "
                + "p.start_date, p.end_date, p.usage_limit, p.status, "
                + "(SELECT COUNT(*) FROM dbo.Booking b "
                + "WHERE b.promotion_id = p.promotion_id "
                + "AND b.status = 'CONFIRMED') AS used_count "
                + "FROM dbo.Promotion p ";
    }

    public List<Promotion> getAll(int cinemaId) {

        List<Promotion> list = new ArrayList<>();

        String sql = getSelectSql()
                + "WHERE p.cinema_id = ? "
                + "ORDER BY p.promotion_id DESC";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPromotion(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Promotion> getPaged(
            String keyword,
            String filter,
            int page,
            int pageSize,
            int cinemaId) {

        List<Promotion> list = new ArrayList<>();

        String sql = getSelectSql()
                + "WHERE p.cinema_id = ? ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += "AND p.promo_code LIKE ? ";
        }

        sql += getFilterCondition(filter);
        sql += "ORDER BY p.promotion_id DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        int offset = (page - 1) * pageSize;

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            int index = 1;

            ps.setInt(index++, cinemaId);

            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword.trim() + "%");
            }

            ps.setInt(index++, offset);
            ps.setInt(index, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPromotion(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public int countFiltered(
            String keyword,
            String filter,
            int cinemaId) {

        String sql = "SELECT COUNT(*) FROM dbo.Promotion p "
                + "WHERE p.cinema_id = ? ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += "AND p.promo_code LIKE ? ";
        }

        sql += getFilterCondition(filter);

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cinemaId);

            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(2, "%" + keyword.trim() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private String getFilterCondition(String filter) {

        if ("AVAILABLE".equals(filter)) {
            return "AND p.status = 'ACTIVE' "
                    + "AND p.start_date <= SYSDATETIME() "
                    + "AND p.end_date >= SYSDATETIME() ";
        }

        if ("UPCOMING".equals(filter)) {
            return "AND p.status = 'ACTIVE' "
                    + "AND p.start_date > SYSDATETIME() ";
        }

        if ("EXPIRED".equals(filter)) {
            return "AND p.status = 'ACTIVE' "
                    + "AND p.end_date < SYSDATETIME() ";
        }

        if ("INACTIVE".equals(filter)) {
            return "AND p.status = 'INACTIVE' ";
        }

        return "";
    }

    public Promotion getById(int promotionId, int cinemaId) {

        String sql = getSelectSql()
                + "WHERE p.promotion_id = ? "
                + "AND p.cinema_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, promotionId);
            ps.setInt(2, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPromotion(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Promotion getByCode(String promoCode, int cinemaId) {

        String sql = getSelectSql()
                + "WHERE p.promo_code = ? "
                + "AND p.cinema_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, promoCode);
            ps.setInt(2, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPromotion(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int countUsageByPromotionId(int promotionId) {

        String sql = "SELECT COUNT(*) FROM dbo.Booking "
                + "WHERE promotion_id = ? "
                + "AND status = 'CONFIRMED'";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, promotionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean existsByCode(String promoCode, int cinemaId) {

        String sql = "SELECT 1 FROM dbo.Promotion "
                + "WHERE promo_code = ? AND cinema_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, promoCode);
            ps.setInt(2, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByCodeExceptId(
            String promoCode,
            int promotionId,
            int cinemaId) {

        String sql = "SELECT 1 FROM dbo.Promotion "
                + "WHERE promo_code = ? "
                + "AND promotion_id <> ? "
                + "AND cinema_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, promoCode);
            ps.setInt(2, promotionId);
            ps.setInt(3, cinemaId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insert(Promotion promotion) {

        String sql = "INSERT INTO dbo.Promotion "
                + "(cinema_id, promo_code, description, discount_type, "
                + "discount_value, min_order_amount, max_discount_amount, "
                + "start_date, end_date, usage_limit, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, promotion.getCinemaId());
            setPromotionData(ps, promotion, 2);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Promotion promotion, int cinemaId) {

        String sql = "UPDATE dbo.Promotion SET "
                + "promo_code = ?, description = ?, discount_type = ?, "
                + "discount_value = ?, min_order_amount = ?, "
                + "max_discount_amount = ?, start_date = ?, end_date = ?, "
                + "usage_limit = ?, status = ? "
                + "WHERE promotion_id = ? AND cinema_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            setPromotionData(ps, promotion, 1);
            ps.setInt(11, promotion.getPromotionId());
            ps.setInt(12, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(
            int promotionId,
            String status,
            int cinemaId) {

        String sql = "UPDATE dbo.Promotion "
                + "SET status = ? "
                + "WHERE promotion_id = ? AND cinema_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, promotionId);
            ps.setInt(3, cinemaId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setPromotionData(
            PreparedStatement ps,
            Promotion promotion,
            int startIndex) throws SQLException {

        ps.setString(startIndex, promotion.getPromoCode());
        ps.setString(startIndex + 1, promotion.getDescription());
        ps.setString(startIndex + 2, promotion.getDiscountType());
        ps.setBigDecimal(startIndex + 3, promotion.getDiscountValue());
        ps.setBigDecimal(startIndex + 4, promotion.getMinOrderAmount());

        if (promotion.getMaxDiscountAmount() == null) {
            ps.setNull(startIndex + 5, Types.DECIMAL);
        } else {
            ps.setBigDecimal(
                    startIndex + 5,
                    promotion.getMaxDiscountAmount());
        }

        ps.setTimestamp(startIndex + 6, promotion.getStartDate());
        ps.setTimestamp(startIndex + 7, promotion.getEndDate());

        if (promotion.getUsageLimit() == null) {
            ps.setNull(startIndex + 8, Types.INTEGER);
        } else {
            ps.setInt(startIndex + 8, promotion.getUsageLimit());
        }

        ps.setString(startIndex + 9, promotion.getStatus());
    }

    private Promotion mapPromotion(ResultSet rs) throws SQLException {

        Promotion promotion = new Promotion();

        promotion.setPromotionId(rs.getInt("promotion_id"));
        promotion.setCinemaId(rs.getInt("cinema_id"));
        promotion.setPromoCode(rs.getString("promo_code"));
        promotion.setDescription(rs.getString("description"));
        promotion.setDiscountType(rs.getString("discount_type"));
        promotion.setDiscountValue(rs.getBigDecimal("discount_value"));
        promotion.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        promotion.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        promotion.setStartDate(rs.getTimestamp("start_date"));
        promotion.setEndDate(rs.getTimestamp("end_date"));

        int usageLimit = rs.getInt("usage_limit");

        if (rs.wasNull()) {
            promotion.setUsageLimit(null);
        } else {
            promotion.setUsageLimit(usageLimit);
        }

        promotion.setUsedCount(rs.getInt("used_count"));
        promotion.setStatus(rs.getString("status"));

        return promotion;
    }
}
