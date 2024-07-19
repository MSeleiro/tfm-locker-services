package com.openfaas.function;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;

public class Handler extends com.openfaas.model.AbstractHandler {

    private final String IP = System.getenv("SIM_IP");
    private final String PORT = System.getenv("SIM_PORT");
    private final String PATH = System.getenv("SIM_PATH");
    private final String BODY_FORMAT = "{\"mid\": %s, \"data\": {\"door\": %s}}";

    public IResponse Handle(IRequest req) {
        Response res = new Response();

        Map<String, String> query = req.getQuery();

        if (query.isEmpty() || !query.containsKey("action")) {
            return badRequest(res);
        }

        JsonObject reqBody = new Gson().fromJson(req.getBody(), JsonObject.class);
        String door = reqBody.get("door").getAsString(); 

        String action = query.get("action").toLowerCase();

        String resp = "";

        if (action.equals("open")) {
            resp = sender("201", door);
        } else if (action.equals("close")) {
            resp = sender("205", door);
        } else {
            return badRequest(res);
        }

        JsonObject body = new JsonObject();
        body.addProperty("resp", resp);
        res.setBody(new Gson().toJson(body));
        res.setContentType("application/json");
	    return res;
    }

    private IResponse badRequest(Response res) {
        res.setBody("No action selected. Use query action=(open, close) in the URI.");
        res.setStatusCode(400);
        return res;
    }

    private String sender(String mid, String door) {
        String body = String.format(BODY_FORMAT, mid, door);

        int httpCode = 0;

        try {
            URL url = new URL("http://" + IP + ':' + PORT + PATH);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            //con.setRequestProperty("Content-Length", String.valueOf(body.getBytes().length));
            con.setDoOutput(true);
            OutputStream outStream = con.getOutputStream();
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
            outStreamWriter.write(body);
            outStreamWriter.flush();
            outStreamWriter.close();
            outStream.close();

            httpCode = con.getResponseCode();
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

            JsonObject resBody = new Gson().fromJson(content.toString(), JsonObject.class);
            return resBody.get("resp").getAsString(); 

        } catch (Exception e) {
            return null;
        }
    }
}
