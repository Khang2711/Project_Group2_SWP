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

    public List<Promotion> getAll() {
        List<Promotion> list = new ArrayList<>();

        String sql = "SELECT promotion_id, promo_code, description, discount_type, "
                + "discount_value, min_order_amount, max_discount_amount, "
                + "start_date, end_date, usage_limit, status "
                + "FROM Promotion ORDER BY promotion_id DESC";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapPromotion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Promotion> getPaged(String keyword, String filter, int page, int pageSize) {
        List<Promotion> list = new ArrayList<>();

        String sql = "SELECT promotion_id, promo_code, description, discount_type, "
                + "discount_value, min_order_amount, max_discount_amount, "
                + "start_date, end_date, usage_limit, status "
                + "FROM Promotion WHERE 1 = 1 ";

        if (keyword != null && !keyword.isEmpty()) {
            sql += "AND promo_code LIKE ? ";
        }

        sql += getFilterCondition(filter);
        sql += "ORDER BY promotion_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        // Calculate how many rows are skipped before the current page
        int offset = (page - 1) * pageSize;

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            int index = 1;

            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
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

    public int countFiltered(String keyword, String filter) {
        String sql = "SELECT COUNT(*) FROM Promotion WHERE 1 = 1 ";

        if (keyword != null && !keyword.isEmpty()) {
            sql += "AND promo_code LIKE ? ";
        }

        sql += getFilterCondition(filter);

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(1, "%" + keyword + "%");
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
            return "AND status = 'ACTIVE' "
                    + "AND start_date <= SYSDATETIME() "
                    + "AND end_date >= SYSDATETIME() ";
        }

        if ("UPCOMING".equals(filter)) {
            return "AND status = 'ACTIVE' "
                    + "AND start_date > SYSDATETIME() ";
        }

        if ("EXPIRED".equals(filter)) {
            return "AND status = 'ACTIVE' "
                    + "AND end_date < SYSDATETIME() ";
        }

        if ("INACTIVE".equals(filter)) {
            return "AND status = 'INACTIVE' ";
        }

        return "";
    }

    public Promotion getById(int promotionId) {
        String sql = "SELECT promotion_id, promo_code, description, discount_type, "
                + "discount_value, min_order_amount, max_discount_amount, "
                + "start_date, end_date, usage_limit, status "
                + "FROM Promotion WHERE promotion_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, promotionId);

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

    public boolean existsByCode(String promoCode) {
        String sql = "SELECT 1 FROM Promotion WHERE promo_code = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, promoCode);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByCodeExceptId(String promoCode, int promotionId) {
        String sql = "SELECT 1 FROM Promotion "
                + "WHERE promo_code = ? AND promotion_id <> ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, promoCode);
            ps.setInt(2, promotionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insert(Promotion promotion) {
        String sql = "INSERT INTO Promotion "
                + "(promo_code, description, discount_type, discount_value, "
                + "min_order_amount, max_discount_amount, start_date, end_date, "
                + "usage_limit, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            setPromotionData(ps, promotion);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Promotion promotion) {
        String sql = "UPDATE Promotion SET "
                + "promo_code = ?, description = ?, discount_type = ?, "
                + "discount_value = ?, min_order_amount = ?, "
                + "max_discount_amount = ?, start_date = ?, end_date = ?, "
                + "usage_limit = ?, status = ? "
                + "WHERE promotion_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            setPromotionData(ps, promotion);
            ps.setInt(11, promotion.getPromotionId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateStatus(int promotionId, String status) {
        String sql = "UPDATE Promotion SET status = ? WHERE promotion_id = ?";

        try (Connection con = new DBContext().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, promotionId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setPromotionData(PreparedStatement ps, Promotion promotion)
            throws SQLException {

        ps.setString(1, promotion.getPromoCode());
        ps.setString(2, promotion.getDescription());
        ps.setString(3, promotion.getDiscountType());
        ps.setBigDecimal(4, promotion.getDiscountValue());
        ps.setBigDecimal(5, promotion.getMinOrderAmount());

        if (promotion.getMaxDiscountAmount() == null) {
            ps.setNull(6, Types.DECIMAL);
        } else {
            ps.setBigDecimal(6, promotion.getMaxDiscountAmount());
        }

        ps.setTimestamp(7, promotion.getStartDate());
        ps.setTimestamp(8, promotion.getEndDate());

        if (promotion.getUsageLimit() == null) {
            ps.setNull(9, Types.INTEGER);
        } else {
            ps.setInt(9, promotion.getUsageLimit());
        }

        ps.setString(10, promotion.getStatus());
    }

    private Promotion mapPromotion(ResultSet rs) throws SQLException {
        Promotion promotion = new Promotion();

        promotion.setPromotionId(rs.getInt("promotion_id"));
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

        promotion.setStatus(rs.getString("status"));

        return promotion;
    }
}
