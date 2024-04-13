package com.openfaas.function;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;

public class Handler extends com.openfaas.model.AbstractHandler {

    private static final String MARIADB_USER = System.getenv("MARIADB_USER");
    private static final String MARIADB_PASS = System.getenv("MARIADB_PASS");

    public IResponse Handle(IRequest req) {
        Response res = new Response();

        String s = "jdbc:mariadb://192.168.1.4:3306/mysql?user="+MARIADB_USER+"&password="+MARIADB_PASS;

        //create connection for a server installed in localhost, with a user "root" with no password
        try (Connection conn = DriverManager.getConnection(s)) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
                try (ResultSet rs = stmt.executeQuery("SELECT 'Hello World!'")) {
                    //position result to first
                    rs.first();
                    res.setBody(rs.getString(1)); //result is "Hello World!"
                }
            }
        } catch(SQLException e) {
            String b = "";
            String path = System.getProperty("java.class.path");
            String[] p;
            p = path.split(":");
            for(int i=0; i< p.length; i++) {
                b += p[i] + "\n";
            }
            res.setBody(
                "Error: " + e.getMessage() + 
                "\n\n" + 
                "Code and State: " + e.getErrorCode() + " - " + e.getSQLState() + 
                "\n\n" + 
                b
                );
        }
	    return res;
    }
}
