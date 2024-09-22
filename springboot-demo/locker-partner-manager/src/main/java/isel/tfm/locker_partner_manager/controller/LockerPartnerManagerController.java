package isel.tfm.locker_partner_manager.controller;

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
public class LockerPartnerManagerController {

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() throws Exception {
		return "Hello from locker-partner-manager microservice!";
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
        Partner p = new Gson().fromJson(body, Partner.class);

        Connection conn = DriverManager.getConnection("");  // removed after testing
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO partners VALUES (?, ?)");
        byte[] uuidBytes = UUIDtoByteArray(p.ptnr_id);

        stmt.setBytes(1, uuidBytes);
        stmt.setString(2, p.ptnr_name);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return p.ptnr_id.toString();
    }

	private String update(String body) throws SQLException {
        Partner p = new Gson().fromJson(body, Partner.class);

        Connection conn = DriverManager.getConnection("");	// removed after testing
        PreparedStatement stmt = conn.prepareStatement("UPDATE partners SET ptnr_name=? WHERE ptnr_id=?");
        byte[] uuidBytes = UUIDtoByteArray(p.ptnr_id);

        stmt.setString(1, p.ptnr_name);
        stmt.setBytes(2, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return p.ptnr_id.toString();
    }
    
    private String delete(String body) throws SQLException {
        Partner p = new Gson().fromJson(body, Partner.class);

        Connection conn = DriverManager.getConnection("");   // removed after testing
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM partners WHERE ptnr_id=?");
        byte[] uuidBytes = UUIDtoByteArray(p.ptnr_id);

        stmt.setBytes(1, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return p.ptnr_id.toString();
    }

    public class Partner {
        public UUID ptnr_id;
        public String ptnr_name;

        Partner() { }

        Partner(String pn) {
            ptnr_id = UUID.randomUUID();
            ptnr_name = pn;
        }

        public UUID getPtnr_id() { return ptnr_id; }
        public String getPtnr_name() { return ptnr_name; }
        public void setPtnr_id(UUID ptnr_id) { this.ptnr_id = ptnr_id; }
        public void setPtnr_name(String ptnr_name) { this.ptnr_name = ptnr_name; }
    }
}
