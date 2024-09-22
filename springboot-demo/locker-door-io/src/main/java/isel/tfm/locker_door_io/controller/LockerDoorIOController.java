package isel.tfm.locker_door_io.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class LockerDoorIOController {
    private final String BODY_FORMAT = "{\"mid\": %s, \"data\": {\"door\": %s}}";

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() throws Exception {
		return "Hello from locker-door-io microservice!";
	}

	@RequestMapping(value = "/code", method = RequestMethod.POST)
	public ResponseEntity<String> code(@RequestBody String body, @RequestParam String action) throws Exception {
		JsonObject reqBody = new Gson().fromJson(body, JsonObject.class);
        String door = reqBody.get("door").getAsString();

        String resp = "";

        if (action.equals("open")) {
            resp = sender("201", door);
        } else if (action.equals("close")) {
            resp = sender("205", door);
        } else {
            return new ResponseEntity<>("No action selected. Use query action=(open, close) in the URI.", HttpStatus.BAD_REQUEST);
        }

        JsonObject respBody = new JsonObject();
        respBody.addProperty("resp", resp);

	    return new ResponseEntity<>(respBody.toString(), HttpStatus.OK);
	}

	private String sender(String mid, String door) {
        String body = String.format(BODY_FORMAT, mid, door);

        int httpCode = 0;

        try {
            URL url = new URL(""); // removed after testing
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
