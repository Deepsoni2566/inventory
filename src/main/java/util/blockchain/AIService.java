package util;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AIService {
    private static final String AI_ENDPOINT = "http://localhost:5005/analyze_product";

    public static String analyzeProduct(String name, String description) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("description", description);

            URL url = new URL(AI_ENDPOINT);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(json.toString().getBytes());
            }

            Scanner scanner = new Scanner(con.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }

            scanner.close();
            return new JSONObject(response.toString()).getString("response");

        } catch (Exception e) {
            return "Error calling AI service: " + e.getMessage();
        }
    }
}
