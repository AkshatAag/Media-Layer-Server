package org.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.Utils.*;

public class Main {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        List<String> legIds = new ArrayList<>();
        executorService.submit(() -> generateCalls(legIds));
        executorService.submit(() -> generateCalls(legIds));
        executorService.submit(() -> generateCalls(legIds));
        executorService.submit(() -> generateCalls(legIds));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.submit(() -> hangupCalls(legIds));
        executorService.submit(() -> hangupCalls(legIds));
    }

    private static void hangupCalls(List<String> legIds) {
        Random random = new Random();
        int maxSize;
        long endTime = System.currentTimeMillis() + DURATION_SEND_CALLS + 60 * 1000;
        while (System.currentTimeMillis() < endTime) {
            maxSize = legIds.size();
            if (maxSize == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }
            int idx = random.nextInt(maxSize);
            String legId = legIds.get(idx);
            legIds.remove(idx);
            String requestBody = getHangupRequest(legId);
            String uri;
            if (random.nextInt(1000) < 500) {//hit baseurl1
                uri = BASE_URL_1 + "/new_event";
            } else {// hit base url 2
                uri = BASE_URL_2 + "/new_event";
            }
            try {
                System.out.println("send hangup call");
                System.out.println(requestBody);
                sendRequest(uri, requestBody);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String getHangupRequest(String legId) {
        return "{\n" + "\"Event-Name\":\"" + "CHANNEL_HANGUP" + "\",\n" + "\"Core-UUID\":\"" + legId + "\"\n" + "}";
    }

    private static void generateCalls(List<String> legIds) {
        Random random = new Random();
        long endTime = System.currentTimeMillis() + DURATION_SEND_CALLS;
        while (System.currentTimeMillis() < endTime) {
            String legId = String.valueOf(random.nextInt(100000) + 1);
            String conversationId = String.valueOf(random.nextInt(100) + 1);
            String requestBody = getGenerateRequest(legId, conversationId);

            legIds.add(legId);
            String uri;
            if (random.nextInt(1000) < 500) {//hit baseurl1
                uri = BASE_URL_1 + "/control_layer/1";
            } else {// hit base url 2
                uri = BASE_URL_2 + "/control_layer/1";
            }
            try {
                System.out.println("send generate call");
                System.out.println(requestBody);
                sendRequest(uri, requestBody);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String getGenerateRequest(String legId, String conversationId) {
        return "{\n" + "    \"legId\":\"" + legId + "\",\n" + "    \"conversationId\":\"" + conversationId + "\"\n" + "}";
    }

    private static void sendRequest(String url, String requestBody) throws IOException {
        COUNTER++;
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

        System.out.println("Response: " + response.toString() + "  counter : " + COUNTER);

        connection.disconnect();
    }
}


