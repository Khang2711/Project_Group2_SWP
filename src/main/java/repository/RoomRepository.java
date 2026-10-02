package repository;

import db.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Room;

/**
 * Provides data access operations for Room.
 *
 * @author Kiet
 */
public class RoomRepository extends DBContext {

    /**
     * Gets all active rooms.
     *
     * @return a list of active rooms
     */
    public List<Room> getAllActive() {

        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT * "
                + "FROM Room "
                + "WHERE status = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "ACTIVE");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int roomId = rs.getInt("room_id");
                int cinemaId = rs.getInt("cinema_id");
                String roomName = rs.getString("room_name");
                int capacity = rs.getInt("capacity");
                String roomType = rs.getString("room_type");
                String status = rs.getString("status");

                rooms.add(new Room(roomId, cinemaId, roomName, capacity, roomType, status));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }
}