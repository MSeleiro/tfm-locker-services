package com.openfaas.function;

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

public class Handler extends com.openfaas.model.AbstractHandler {

    private static final String MARIADB_USER = System.getenv("MARIADB_USER");
    private static final String MARIADB_PASS = System.getenv("MARIADB_PASS");
    private static final String USER_DB = System.getenv("USER_DB");

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

        String s = "jdbc:mariadb://192.168.1.4:3306/"+USER_DB+"?user="+MARIADB_USER+"&password="+MARIADB_PASS;

        //create connection for a server installed in localhost, with a user "root" with no password
        try (Connection conn = DriverManager.getConnection(s)) {
            // create a Statement
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?)")) {
                User u = new User("Test Test");
                byte[] uuidBytes = UUIDtoByteArray(u.user_id);

                stmt.setBytes(1, uuidBytes);
                stmt.setString(2, u.user_name);
                stmt.executeUpdate();
                
                res.setBody("Inverse?: " + (u.user_id.equals(byteArraytoUUID(uuidBytes))));
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

    public class User {
        public UUID user_id;
        public String user_name;

        User(String un) {
            user_id = UUID.randomUUID();
            user_name = un;
        }
    }
}
