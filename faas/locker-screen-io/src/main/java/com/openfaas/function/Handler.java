package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Handler extends com.openfaas.model.AbstractHandler {

    private final String OPENFAAS_URL = "http://192.168.1.4:8080/function/";
    private final String MAIN_CONTROLLER = "locker-controller";

    public IResponse Handle(IRequest req) {
        Response r = new Response();

        String jsonScreenCode = req.getBody(); // might need extra step to create new JSON depending on how the code arrives to this section

        String res = sendToController(jsonScreenCode);
        if (res == null) {
            r.setBody("sendToContoller error");
            r.setStatusCode(500);
            return r;
        }

        display(res);

        return r;
    }

    // Sends code received from the touchscreen to the main controller for processing 
    // Expects new screen ID to display new information
    private String sendToController(String json) {
        try {
            URL url = new URL(OPENFAAS_URL + MAIN_CONTROLLER);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            OutputStream outStream = con.getOutputStream();
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
            outStreamWriter.write(json);
            outStreamWriter.flush();
            outStreamWriter.close();
            outStream.close();

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

    // Displays new information on the screen based on the main controller response
    private void display(String screenID) {
        // TODO possibly not in the range of this study
    }
}