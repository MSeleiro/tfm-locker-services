package com.openfaas.function;

import java.util.Map;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Handler extends com.openfaas.model.AbstractHandler {

    private static final String MARIADB_USER = System.getenv("MARIADB_USER");
    private static final String MARIADB_PASS = System.getenv("MARIADB_PASS");
    private static final String DB = System.getenv("DB");
    private static final String CONN_STRING = String.format( 
        "%s%s:%s/%s?user=%s&password=%s",
        System.getenv("JDBC_DRIVER"),
        System.getenv("RPI_ADDR"),
        System.getenv("FAASD_PORT"),
        DB,
        MARIADB_USER,
        MARIADB_PASS
    );

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

    public IResponse Handle(IRequest req) {
        Response res = new Response();

        Map<String, String> query = req.getQuery();

        if (query.isEmpty() || !query.containsKey("action")) {
            return badRequest(res);
        }

        JsonObject body = new JsonObject();
        try {
            switch (query.get("action").toLowerCase()) {
                case "insert"   : {
                    String id = insert(req.getBody()); 
                    body.addProperty("id", id);
                    break;
                }
                case "update"   : {
                    String id = update(req.getBody()); 
                    body.addProperty("id", id);
                    break;
                }
                case "delete"   : {
                    String id = delete(req.getBody());
                    body.addProperty("id", id);
                    break;
                }
                case "validate" : {
                    int door = validate(req.getBody());
                    body.addProperty("door", door);
                    break;
                }
                case "disable": {
                    String status = disable(req.getBody());
                    body.addProperty("status", status);
                    break;
                }
                default : badRequest(res);
            }
        } catch(Exception e) {
            res.setBody(
                "Error: " + e.getMessage() + 
                "\n\n" //+ 
                //"Code and State: " + e.getErrorCode() + " - " + e.getSQLState()
            );
        }
 
        res.setBody(new Gson().toJson(body));
        res.setContentType("application/json");
	    return res;
    }

    private int validate(String body) throws SQLException {
        JsonObject j = new Gson().fromJson(body, JsonObject.class);
        Connection conn = DriverManager.getConnection(CONN_STRING);
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
        Connection conn = DriverManager.getConnection(CONN_STRING);
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

        Connection conn2 = DriverManager.getConnection(CONN_STRING);

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

        Connection conn = DriverManager.getConnection(CONN_STRING);
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

        Connection conn = DriverManager.getConnection(CONN_STRING);
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

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM reservations WHERE rsv_id=?");
        byte[] uuidBytes1 = UUIDtoByteArray(r.rsv_id);

        stmt.setBytes(1, uuidBytes1);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
        return r.rsv_id.toString();
    }
    
    private IResponse badRequest(Response res) {
        res.setBody("No action selected. Use query action=(insert, update, delete) in the URI.");
        res.setStatusCode(400);
        return res;
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
