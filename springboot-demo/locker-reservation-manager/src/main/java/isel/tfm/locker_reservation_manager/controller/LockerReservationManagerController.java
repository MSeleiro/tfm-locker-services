package isel.tfm.locker_reservation_manager.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.UUID;
import java.util.Map;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class LockerReservationManagerController {

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() throws Exception {
		return "Hello from locker-reservation-manager microservice!";
	}

	@RequestMapping(value = "/code", method = RequestMethod.POST)
	public ResponseEntity<String> code(@RequestBody String body, @RequestParam String action) throws Exception {
		JsonObject res = new JsonObject();
        try {
            switch (action.toLowerCase()) {
                case "insert": {
                    String id = insert(body); 
                    res.addProperty("id", id);
                    break;
                }
                case "update": {
                    String id = update(body); 
                    res.addProperty("id", id);
                    break;
                }
                case "delete": {
                    String id = delete(body);
                    res.addProperty("id", id);
                    break;
                }
				case "validate": {
                    int door = validate(body);
                    res.addProperty("door", door);
                    break;
                }
                case "disable": {
                    String status = disable(body);
                    res.addProperty("status", status);
                    break;
                }
                default: return new ResponseEntity<>("No action selected. Use query action=(insert, update, delete, validate, disable) in the URI.", HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            return new ResponseEntity<>("Error" + sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
 
	    return new ResponseEntity<>(res.toString(), HttpStatus.OK);
	}

	public static byte[] UUIDtoByteArray(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

    public static UUID byteArraytoUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long h = bb.getLong();
        long l = bb.getLong();
        return new UUID(h, l);
    }


    private int validate(String body) throws SQLException {
        JsonObject j = new Gson().fromJson(body, JsonObject.class);
        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("SELECT rsv_door FROM reservations WHERE rsv_dcode=? OR rsv_lcode=? OR rsv_ccode=?");

        String hash = j.get("hash").getAsString();
        stmt.setString(1, hash);
        stmt.setString(2, hash);
        stmt.setString(3, hash);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next())
            return -1;

        int ret = rs.getInt("rsv_door");

        stmt.close();
        conn.close();
        return ret;
    }

    private String disable(String body) throws SQLException {
        JsonObject j = new Gson().fromJson(body, JsonObject.class);
        Connection conn = DriverManager.getConnection("");  // removed after testing
        String status = "";

        PreparedStatement stmt = conn.prepareStatement("SELECT rsv_id, rsv_dcode, rsv_lcode FROM reservations WHERE rsv_dcode=? OR rsv_lcode=? OR rsv_ccode=?");

        String hash = j.get("hash").getAsString();
        stmt.setString(1, hash);
        stmt.setString(2, hash);
        stmt.setString(3, hash);
        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) {
            stmt.close();
            conn.close();
            return "";
        }

        byte[] uuid = rs.getBytes("rsv_id");
        String dcode = rs.getString("rsv_dcode");
        String lcode = rs.getString("rsv_lcode");

        stmt.close();
        conn.close();

        Connection conn2 = DriverManager.getConnection("");  // removed after testing

        StringBuilder st = new StringBuilder("UPDATE reservations SET rsv_status=?, ");
        if (hash.equals(dcode)) {
            status = "IN_STORAGE";
            st.append("rsv_dcode=? ");
        } else if (hash.equals(lcode)) {
            status = "RETRIVED";
            st.append("rsv_lcode=? ");
        } else {
            status = "IN_STORAGE";
            st.append("rsv_ccode=? ");
        }
        st.append("WHERE rsv_id=?");

        PreparedStatement stmt2 = conn2.prepareStatement(st.toString());

        stmt2.setString(1, status);
        stmt2.setString(2, "!"+hash);
        stmt2.setBytes(3, uuid);
        stmt2.executeUpdate();

        stmt2.close();
        conn2.close();

        return status;
    }

	private String insert(String body) throws SQLException {
        Reservation r = new Gson().fromJson(body, Reservation.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO reservations VALUES (?, ?, ?, ?, ?, ?, ?)");
        byte[] uuidBytes1 = UUIDtoByteArray(r.rsv_id);
        byte[] uuidBytes2 = UUIDtoByteArray(r.rsv_user);

        stmt.setBytes(1, uuidBytes1);
        stmt.setString(2, r.rsv_status);
        stmt.setBytes(3, uuidBytes2);
        stmt.setInt(4, r.rsv_door);
        stmt.setString(5, r.rsv_dcode);
        stmt.setString(6, r.rsv_lcode);
        stmt.setString(7, r.rsv_ccode);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return r.rsv_id.toString();
    }

    private String update(String body) throws SQLException {
        Reservation r = new Gson().fromJson(body, Reservation.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("UPDATE reservations SET rsv_status=?, rsv_user=?, rsv_door=?, rsv_dcode=?, rsv_lcode=?, rsv_ccode=? WHERE rsv_id=?");
        byte[] uuidBytes1 = UUIDtoByteArray(r.rsv_id);
        byte[] uuidBytes2 = UUIDtoByteArray(r.rsv_user);

        stmt.setString(1, r.rsv_status);
        stmt.setBytes(2, uuidBytes2);
        stmt.setInt(3, r.rsv_door);
        stmt.setString(4, r.rsv_dcode);
        stmt.setString(5, r.rsv_lcode);
        stmt.setString(6, r.rsv_ccode);
        stmt.setBytes(7, uuidBytes1);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
        return r.rsv_id.toString();
    }
    
    private String delete(String body) throws SQLException {
        Reservation r = new Gson().fromJson(body, Reservation.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM reservations WHERE rsv_id=?");
        byte[] uuidBytes1 = UUIDtoByteArray(r.rsv_id);

        stmt.setBytes(1, uuidBytes1);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
        return r.rsv_id.toString();
    }
	
	public class Reservation {
        public UUID rsv_id;
        public String rsv_status;
        public UUID rsv_user;
        public int rsv_door;
        public String rsv_dcode;
        public String rsv_lcode;
        public String rsv_ccode;

        Reservation() { }

        public UUID getRsv_id() { return rsv_id; }
        public String getRsv_status() { return rsv_status; }
        public UUID getRsv_user() { return rsv_user; }
        public int getRsv_door() { return rsv_door; }
        public String getRsv_dcode() { return rsv_dcode; }
        public String getRsv_lcode() { return rsv_lcode; }
        public String getRsv_ccode() { return rsv_ccode; }
        public void setRsv_id(UUID rsv_id) { this.rsv_id = rsv_id; }
        public void setRsv_status(String rsv_status) { this.rsv_status = rsv_status; }
        public void setRsv_user(UUID rsv_user) { this.rsv_user = rsv_user; }
        public void setRsv_door(int rsv_door) { this.rsv_door = rsv_door; }
        public void setRsv_dcode(String rsv_dcode) { this.rsv_dcode = rsv_dcode; }
        public void setRsv_lcode(String rsv_lcode) { this.rsv_lcode = rsv_lcode; }
        public void setRsv_ccode(String rsv_ccode) { this.rsv_ccode = rsv_ccode; }
    }
}
