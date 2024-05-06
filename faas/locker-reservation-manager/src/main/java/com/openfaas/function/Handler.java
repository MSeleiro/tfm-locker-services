package com.openfaas.function;

import java.util.Map;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import com.google.gson.Gson;

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

        try {
            switch (query.get("action").toLowerCase()) {
                case "insert" : insert(req.getBody()); break;
                case "update" : update(req.getBody()); break;
                case "delete" : delete(req.getBody()); break;
                default : badRequest(res);
            }
        } catch(Exception e) {
            res.setBody(
                "Error: " + e.getMessage() + 
                "\n\n" //+ 
                //"Code and State: " + e.getErrorCode() + " - " + e.getSQLState()
            );
        }
 
	    return res;
    }

    private void insert(String body) throws SQLException {
        Reservation r = new Gson().fromJson(body, Reservation.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO reservations VALUES (?, ?, ?, ?)");
        byte[] uuidBytes1 = UUIDtoByteArray(r.rsv_id);
        byte[] uuidBytes2 = UUIDtoByteArray(r.rsv_user);

        stmt.setBytes(1, uuidBytes1);
        stmt.setString(2, r.rsv_status);
        stmt.setBytes(3, uuidBytes2);
        stmt.setInt(4, r.rsv_door);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private void update(String body) throws SQLException {
        Reservation r = new Gson().fromJson(body, Reservation.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("UPDATE reservations SET rsv_status=?, rsv_user=?, rsv_door=? WHERE rsv_id=?");
        byte[] uuidBytes1 = UUIDtoByteArray(r.rsv_id);
        byte[] uuidBytes2 = UUIDtoByteArray(r.rsv_user);

        stmt.setString(1, r.rsv_status);
        stmt.setBytes(2, uuidBytes2);
        stmt.setInt(3, r.rsv_door);
        stmt.setBytes(4, uuidBytes1);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }
    
    private void delete(String body) throws SQLException {
        Reservation r = new Gson().fromJson(body, Reservation.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM reservations WHERE rsv_id=?");
        byte[] uuidBytes1 = UUIDtoByteArray(r.rsv_id);

        stmt.setBytes(1, uuidBytes1);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
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

        Reservation() { }

        public UUID getRsv_id() { return rsv_id; }
        public String getRsv_status() { return rsv_status; }
        public UUID getRsv_user() { return rsv_user; }
        public int getRsv_door() { return rsv_door; }
        public void setRsv_id(UUID rsv_id) { this.rsv_id = rsv_id; }
        public void setRsv_status(String rsv_status) { this.rsv_status = rsv_status; }
        public void setRsv_user(UUID rsv_user) { this.rsv_user = rsv_user; }
        public void setRsv_door(int rsv_door) { this.rsv_door = rsv_door; }
    }
}
