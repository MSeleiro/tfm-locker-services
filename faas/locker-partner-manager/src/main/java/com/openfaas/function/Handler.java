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
                case "insert": {
                    String id = insert(req.getBody()); 
                    body.addProperty("id", id);
                    break;
                }
                case "update": {
                    String id = update(req.getBody()); 
                    body.addProperty("id", id);
                    break;
                }
                case "delete": {
                    String id = delete(req.getBody());
                    body.addProperty("id", id);
                    break;
                }
                default: return badRequest(res);
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

    private String insert(String body) throws SQLException {
        Partner p = new Gson().fromJson(body, Partner.class);

        Connection conn = DriverManager.getConnection(CONN_STRING);
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

        Connection conn = DriverManager.getConnection(CONN_STRING);
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

        Connection conn = DriverManager.getConnection(CONN_STRING);
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM partners WHERE ptnr_id=?");
        byte[] uuidBytes = UUIDtoByteArray(p.ptnr_id);

        stmt.setBytes(1, uuidBytes);
        stmt.executeUpdate();

        stmt.close();
        conn.close();

        return p.ptnr_id.toString();
    }
    
    private IResponse badRequest(Response res) {
        res.setBody("No action selected. Use query action=(insert, update, delete) in the URI.");
        res.setStatusCode(400);
        return res;
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
