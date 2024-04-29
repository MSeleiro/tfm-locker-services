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
    private static final String USER_DB = System.getenv("USER_DB");
    private static final String CONN_STRING = String.format( 
        "%s%s:%s/%s?user=%s&password=%s",
        System.getenv("JDBC_DRIVER"),
        System.getenv("RPI_ADDR"),
        System.getenv("FAASD_PORT"),
        USER_DB,
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
        User u = new Gson().fromJson(body, User.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?)");
        byte[] uuidBytes = UUIDtoByteArray(u.user_id);

        stmt.setBytes(1, uuidBytes);
        stmt.setString(2, u.user_name);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private void update(String body) throws SQLException {
        User u = new Gson().fromJson(body, User.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("UPDATE users SET user_name=? WHERE user_id=?");
        byte[] uuidBytes = UUIDtoByteArray(u.user_id);

        stmt.setString(1, u.user_name);
        stmt.setBytes(2, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }
    
    private void delete(String body) throws SQLException {
        User u = new Gson().fromJson(body, User.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id=?");
        byte[] uuidBytes = UUIDtoByteArray(u.user_id);

        stmt.setBytes(1, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }
    
    private IResponse badRequest(Response res) {
        res.setBody("No action selected. Use query action=(insert, update, delete) in the URI.");
        res.setStatusCode(400);
        return res;
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
