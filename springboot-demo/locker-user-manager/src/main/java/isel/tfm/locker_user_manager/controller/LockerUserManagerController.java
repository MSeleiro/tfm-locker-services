package isel.tfm.locker_user_manager.controller;

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
public class LockerUserManagerController {

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() throws Exception {
		return "Hello from locker-user-manager microservice!";
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
        User u = new Gson().fromJson(body, User.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?)");
        byte[] uuidBytes = UUIDtoByteArray(u.user_id);

        stmt.setBytes(1, uuidBytes);
        stmt.setString(2, u.user_name);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
        return u.user_id.toString();
    }

    private String update(String body) throws SQLException {
        User u = new Gson().fromJson(body, User.class);

        Connection conn = DriverManager.getConnection(""); // removed after testing 
        PreparedStatement stmt = conn.prepareStatement("UPDATE users SET user_name=? WHERE user_id=?");
        byte[] uuidBytes = UUIDtoByteArray(u.user_id);

        stmt.setString(1, u.user_name);
        stmt.setBytes(2, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
        return u.user_id.toString();
    }
    
    private String delete(String body) throws SQLException {
        User u = new Gson().fromJson(body, User.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id=?");
        byte[] uuidBytes = UUIDtoByteArray(u.user_id);

        stmt.setBytes(1, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return u.user_id.toString();
    }

    public class User {
        public UUID user_id;
        public String user_name;

        User() { }

        User(String un) {
            user_id = UUID.randomUUID();
            user_name = un;
        }

        public UUID getUser_id() { return user_id; }
        public String getUser_name() { return user_name; }
        public void setUser_id(UUID user_id) { this.user_id = user_id; }
        public void setUser_name(String user_name) { this.user_name = user_name; }
    }
}
