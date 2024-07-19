package com.openfaas.function;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Handler extends com.openfaas.model.AbstractHandler {

    private final String OPENFAAS_URL = System.getenv("OPENFAAS_URL");
    private final String MAIN_CONTROLLER = System.getenv("MAIN_CONTROLLER");;

    public IResponse Handle(IRequest req) {
        Response res = new Response();

        JsonObject body = new Gson().fromJson(req.getBody(), JsonObject.class);
        String code = body.get("code").getAsString();

        int httpCode;
        try {
            httpCode = sendToController(code);

            if (httpCode == 500) {
                res.setBody("sendToContoller error");
                res.setStatusCode(500);
                return res;
            }

            display(httpCode == 200 ? 0 : 1);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            res.setBody(
                "Error" + sw.toString()
            );
            res.setStatusCode(500);
            return res;
        }

        res.setBody(String.valueOf(httpCode));
        res.setStatusCode(200);
        return res;
    }

    // Sends code received from the touchscreen to the main controller for processing 
    // Expects new screen ID to display new information
    private int sendToController(String code) throws Exception{  
        JsonObject body = new JsonObject();
        body.addProperty("code", code);            

        URL url = new URL(OPENFAAS_URL + MAIN_CONTROLLER);
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

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return httpCode;
    }

    // Displays new information on the screen based on the main controller response
    private void display(int screenID) {
        // TODO possibly not in the range of this study

        /*
        if (screenID == 0) {
            display message: thanks for using our service
        } else {
            display message: invalid code, try another 
        }
        */
    }
}