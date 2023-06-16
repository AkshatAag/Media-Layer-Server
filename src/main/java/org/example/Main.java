package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.Utils.*;

public class Main {
    public static void main(String[] args) throws IOException {
        processEventsFromFile(FILE_PATH);
    }

    private static void processEventsFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process the line
                if (line.trim().isEmpty()) return;
                if (line.startsWith("Event-Name:")) {
                    String requestBody = "";
                    LocalDateTime eventTimeStamp = null;
                    while (!line.trim().isEmpty()) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();

                            if (key.equals("Event-Date-Local")) {
                                eventTimeStamp = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
                            }

                            // Append the key-value pair to the request body
                            requestBody += "\"" + key + "\": \"" + value + "\",";
                        }
                        if ((line = reader.readLine()) == null) break;
                    }
                    if (eventTimeStamp != null) {
                        // Remove the trailing comma from the request body
                        requestBody = requestBody.substring(0, requestBody.length() - 1);
                        requestBody = "{" + requestBody + "}";


                        try {
                            sendPostRequest(API_URL, requestBody);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendPostRequest(String url, String requestBody) throws IOException {
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.writeBytes(requestBody);
            outputStream.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        System.out.println("Response: " + response.toString());

        connection.disconnect();
    }
}


