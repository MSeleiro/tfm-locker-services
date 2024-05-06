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
        } catch(SQLException e) {
            res.setBody(
                "Error: " + e.getMessage() + 
                "\n\n" + 
                "Code and State: " + e.getErrorCode() + " - " + e.getSQLState()
            );
        }
 
	    return res;
    }

    private void insert(String body) throws SQLException {
        Door d = new Gson().fromJson(body, Door.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO doors VALUES (?, ?, ?, ?, ?)");
        byte[] uuidBytes = UUIDtoByteArray(d.door_ptnr);

        stmt.setInt(1, d.door_id);
        stmt.setString(2, d.door_type);
        stmt.setBoolean(3, d.door_open);
        stmt.setBoolean(4, d.occupied);
        stmt.setBytes(5, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private void update(String body) throws SQLException {
        Door d = new Gson().fromJson(body, Door.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("UPDATE doors SET door_type=?, door_open=?, occupied=?, door_ptnr=? WHERE door_id=?");
        byte[] uuidBytes = UUIDtoByteArray(d.door_ptnr);

        stmt.setString(1, d.door_type);
        stmt.setBoolean(2, d.door_open);
        stmt.setBoolean(3, d.occupied);
        stmt.setBytes(4, uuidBytes);
        stmt.setInt(5, d.door_id);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }
    
    private void delete(String body) throws SQLException {
        Door d = new Gson().fromJson(body, Door.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM doors WHERE door_id=?");

        stmt.setInt(1, d.door_id);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }
    
    private IResponse badRequest(Response res) {
        res.setBody("No action selected. Use query action=(insert, update, delete) in the URI.");
        res.setStatusCode(400);
        return res;
    }

    public class Door {
        public int door_id;
        public String door_type;
        public boolean door_open;
        public boolean occupied;
        public UUID door_ptnr;

        Door() { }

        public int getDoor_id() { return door_id; }
        public UUID getDoor_ptnr() { return door_ptnr; }
        public String getDoor_type() { return door_type; }
        public boolean getDoor_open() { return door_open; }
        public boolean getOccupied() { return occupied; }
        public void setDoor_id(int door_id) { this.door_id = door_id; }
        public void setDoor_open(boolean door_open) { this.door_open = door_open; }
        public void setDoor_ptnr(UUID door_ptnr) { this.door_ptnr = door_ptnr; }
        public void setDoor_type(String door_type) { this.door_type = door_type; }
        public void setOccupied(boolean occupied) { this.occupied = occupied; }
    }
}
