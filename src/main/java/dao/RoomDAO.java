package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Room;
import utils.DBUtils;

public class RoomDAO {

    private Room mapRoom(ResultSet rs) throws Exception {
        return new Room(
                rs.getString("roomID"),
                rs.getString("roomName"),
                rs.getInt("totalSeats"),
                rs.getBoolean("status")
        );
    }

    public List<Room> getAllRooms() throws Exception {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT roomID, roomName, totalSeats, status FROM tblRooms ORDER BY roomID";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                list.add(mapRoom(rs));
            }
        }
        return list;
    }

    public List<Room> getActiveRooms() throws Exception {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT roomID, roomName, totalSeats, status FROM tblRooms WHERE status = 1 ORDER BY roomID";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql);
                ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                list.add(mapRoom(rs));
            }
        }
        return list;
    }

    public Room getRoomByID(String roomID) throws Exception {
        String sql = "SELECT roomID, roomName, totalSeats, status FROM tblRooms WHERE roomID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, roomID);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapRoom(rs);
                }
            }
        }
        return null;
    }

    public boolean createRoom(Room room) throws Exception {
        String sql = "INSERT INTO tblRooms(roomID, roomName, totalSeats, status) VALUES(?, ?, ?, ?)";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, room.getRoomID());
            stm.setString(2, room.getRoomName());
            stm.setInt(3, room.getTotalSeats());
            stm.setBoolean(4, room.isStatus());
            return stm.executeUpdate() > 0;
        }
    }

    public boolean updateRoom(Room room) throws Exception {
        String sql = "UPDATE tblRooms SET roomName = ?, totalSeats = ?, status = ? WHERE roomID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, room.getRoomName());
            stm.setInt(2, room.getTotalSeats());
            stm.setBoolean(3, room.isStatus());
            stm.setString(4, room.getRoomID());
            return stm.executeUpdate() > 0;
        }
    }

    public boolean updateRoomStatus(String roomID, boolean status) throws Exception {
        String sql = "UPDATE tblRooms SET status = ? WHERE roomID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setBoolean(1, status);
            stm.setString(2, roomID);
            return stm.executeUpdate() > 0;
        }
    }
}
