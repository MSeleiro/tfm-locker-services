package isel.tfm.locker_door_manager.controller;

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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class LockerDoorManagerController {

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() throws Exception {
		return "Hello from locker-door-manager microservice!";
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
                default: return new ResponseEntity<>("No action selected. Use query action=(insert, update, delete) in the URI.", HttpStatus.BAD_REQUEST);
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

    private String insert(String body) throws SQLException {
        Door d = new Gson().fromJson(body, Door.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO doors VALUES (?, ?, ?)");
        byte[] uuidBytes = UUIDtoByteArray(d.door_ptnr);

        stmt.setInt(1, d.door_id);
        stmt.setString(2, d.door_type);
        stmt.setBytes(3, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return String.valueOf(d.door_id);
    }

    private String update(String body) throws SQLException {
        Door d = new Gson().fromJson(body, Door.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("UPDATE doors SET door_type=?, door_ptnr=? WHERE door_id=?");
        byte[] uuidBytes = UUIDtoByteArray(d.door_ptnr);

        stmt.setString(1, d.door_type);
        stmt.setBytes(2, uuidBytes);
        stmt.setInt(3, d.door_id);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return String.valueOf(d.door_id);
    }
    
    private String delete(String body) throws SQLException {
        Door d = new Gson().fromJson(body, Door.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM doors WHERE door_id=?");

        stmt.setInt(1, d.door_id);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return String.valueOf(d.door_id);
    }

	public class Door {
        public int door_id;
        public String door_type;
        public UUID door_ptnr;

        Door() { }

        public int getDoor_id() { return door_id; }
        public UUID getDoor_ptnr() { return door_ptnr; }
        public String getDoor_type() { return door_type; }
        public void setDoor_id(int door_id) { this.door_id = door_id; }
        public void setDoor_ptnr(UUID door_ptnr) { this.door_ptnr = door_ptnr; }
        public void setDoor_type(String door_type) { this.door_type = door_type; }
    }
}
