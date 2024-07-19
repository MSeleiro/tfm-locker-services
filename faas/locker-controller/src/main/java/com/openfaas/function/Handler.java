package com.openfaas.function;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;

public class Handler extends com.openfaas.model.AbstractHandler {

    private final String OPENFAAS_URL = System.getenv("OPENFAAS_URL");
    private final String BACKEND_URL = System.getenv("BACKEND_URL");
    private final String RESERVATION_MANAGER = System.getenv("RESERVATIONS_DB_FUNC");
    private final String DOOR_IO = System.getenv("DOOR_IO_HANDLER");

    public IResponse Handle(IRequest req) {
        Response res = new Response();
        try {
            JsonObject body = new Gson().fromJson(req.getBody(), JsonObject.class);
            String c = body.get("code").getAsString();
            String sha256 = Hashing.sha256()
                .hashString(c, StandardCharsets.UTF_8)
                .toString();

            int door = validateCode(sha256);

            if (door == -2) {
                res.setBody("validate error");
                res.setStatusCode(500);
                return res;
            }

            if (door == -1) {
                res.setBody("Controller: Invalid code");

                /*
                * https://www.rfc-editor.org/rfc/rfc4918#section-11.2
                * The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity 
                * (hence a 415 Unsupported Media Type status code is inappropriate), and the syntax of the request entity is correct 
                * (thus a 400 Bad Request status code is inappropriate) but was unable to process the contained instructions. 
                * For example, this error condition may occur if an XML request body contains well-formed (i.e., syntactically correct), 
                * but semantically erroneous, XML instructions.
                */
                res.setStatusCode(422); 
                return res;
            }
            

            boolean opened = openDoor(door);

            if (!opened) {
                res.setBody("openDoor error");
                res.setStatusCode(500);
                return res;
            }
            
            boolean closed = closeDoor(door);

            if (!closed) {
                res.setBody("closeDoor error");
                res.setStatusCode(500);
                return res;
            }
    
            String reservationStatus = disableCode(sha256); //also updates package status

            if (reservationStatus == null || reservationStatus == "") {
                res.setBody("disableCode error");
                res.setStatusCode(500);
                return res;
            }

            triggerBackendUpdate(reservationStatus);

            res.setBody("Operation successful");
            res.setStatusCode(200);
            return res;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            res.setBody(
                "Error" + sw.toString()
            );
            res.setStatusCode(500);
            return res;
        }
    }

    // verify if code exists in the database
    private int validateCode(String sha256) {
        String res = sender(OPENFAAS_URL, RESERVATION_MANAGER, "validate", "hash", sha256);

        if (res == null)
            return -2;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("door").getAsInt(); 
    }

    // opens door associated with the code
    private boolean openDoor(int door) {
        String res = sender(OPENFAAS_URL, DOOR_IO, "open", "door", String.valueOf(door));

        if (res == null)
            return false;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("resp").getAsInt() == 0; 
    }

    private boolean closeDoor(int door) {
        String res = sender(OPENFAAS_URL, DOOR_IO, "close", "door", String.valueOf(door));

        if (res == null)
            return false;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("resp").getAsInt() == 0; 
    }

    private String disableCode(String sha256) {
        String res = sender(OPENFAAS_URL, RESERVATION_MANAGER, "disable", "hash", sha256);

        if (res == null)
            return null;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("status").getAsString(); 
    }

    private void triggerBackendUpdate(String update) {
        //sender(BACKEND_URL, "", null, null, update);
    }

    // sends out requests to other functions
    private String sender(String baseURL, String path, String action, String property, String value) {
        StringBuilder urlBuilder = new StringBuilder(baseURL);
        urlBuilder.append(path);
        if (action != null) {
            urlBuilder.append("?action=");
            urlBuilder.append(action);
        }

        JsonObject body;
        if (property != null) {
            body = new JsonObject();
            body.addProperty(property, value);
        } else {
            body = new Gson().fromJson(value, JsonObject.class);
        }

        try {
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            OutputStream outStream = con.getOutputStream();
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
            outStreamWriter.write(new Gson().toJson(body));
            outStreamWriter.flush();
            outStreamWriter.close();
            outStream.close();

            int httpCode = con.getResponseCode();
            if (httpCode != 200) {
                con.disconnect();
                return null;    
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            return content.toString();

        } catch (Exception e) {
            return null;
        }
    }
}
