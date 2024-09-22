package isel.tfm.locker_screen_io.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class LockerScreenIOController {

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() throws Exception {
		return "Hello from locker_screen_io microservice!";
	}

	@RequestMapping(value = "/code", method = RequestMethod.POST)
	public ResponseEntity<String> code(@RequestBody String body) throws Exception {
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
        String code = jsonBody.get("code").getAsString();

        int httpCode;
        try {
            httpCode = sendToController(code);

            if (httpCode == 500) {
                return new ResponseEntity<>("sendToContoller error", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            display(httpCode == 200 ? 0 : 1);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            return new ResponseEntity<>("Error" + sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(String.valueOf(httpCode), HttpStatus.OK);
	}

	    // Sends code received from the touchscreen to the main controller for processing 
    // Expects new screen ID to display new information
    private int sendToController(String code) throws Exception{  
        JsonObject body = new JsonObject();
        body.addProperty("code", code);            

        URL url = new URL("http://localhost:8110/locker-controller/code");
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
