package isel.tfm.locker_controller.controller;

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
import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class LockerControllerController {

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() throws Exception {
		return "Hello from locker-controller microservice!";
	}

	@RequestMapping(value = "/code", method = RequestMethod.POST)
	public ResponseEntity<String> code(@RequestBody String body) throws Exception {
		try {
            JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
            String c = jsonBody.get("code").getAsString();
            String sha256 = Hashing.sha256()
                .hashString(c, StandardCharsets.UTF_8)
                .toString();

            int door = validateCode(sha256);

            if (door == -2) {
				return new ResponseEntity<>("validate error", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (door == -1) {
                /*
                * https://www.rfc-editor.org/rfc/rfc4918#section-11.2
                * The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity 
                * (hence a 415 Unsupported Media Type status code is inappropriate), and the syntax of the request entity is correct 
                * (thus a 400 Bad Request status code is inappropriate) but was unable to process the contained instructions. 
                * For example, this error condition may occur if an XML request body contains well-formed (i.e., syntactically correct), 
                * but semantically erroneous, XML instructions.
                */
				return new ResponseEntity<>("Controller: Invalid code", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            

            boolean opened = openDoor(door);

            if (!opened) {
				return new ResponseEntity<>("openDoor error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            boolean closed = closeDoor(door);

            if (!closed) {
				return new ResponseEntity<>("closeDoor error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
    
            String reservationStatus = disableCode(sha256); //also updates package status

            if (reservationStatus == null || reservationStatus == "") {
				return new ResponseEntity<>("disableCode error", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            triggerBackendUpdate(reservationStatus);

            return new ResponseEntity<>("Operation successful", HttpStatus.OK);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

			return new ResponseEntity<>("Error" + sw.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	// verify if code exists in the database
    private int validateCode(String sha256) {
        String res = sender("http://localhost:8116/locker-reservation-manager/code", "validate", "hash", sha256);

        if (res == null)
            return -2;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("door").getAsInt(); 
    }

    // opens door associated with the code
    private boolean openDoor(int door) {
        String res = sender("http://localhost:8111/locker-door-io/code", "open", "door", String.valueOf(door));

        if (res == null)
            return false;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("resp").getAsInt() == 0; 
    }

    private boolean closeDoor(int door) {
        String res = sender("http://localhost:8111/locker-door-io/code", "close", "door", String.valueOf(door));

        if (res == null)
            return false;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("resp").getAsInt() == 0; 
    }

    private String disableCode(String sha256) {
        String res = sender("http://localhost:8116/locker-reservation-manager/code", "disable", "hash", sha256);

        if (res == null)
            return null;

        JsonObject resBody = new Gson().fromJson(res, JsonObject.class);
        return resBody.get("status").getAsString(); 
    }

    private void triggerBackendUpdate(String update) {
        //sender(BACKEND_URL, ...);
    }

    // sends out requests to other functions
    private String sender(String urlString, String action, String property, String value) {
        StringBuilder urlBuilder = new StringBuilder(urlString);
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
