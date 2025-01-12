package moe.ono.util.analytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import moe.ono.util.Logger;

public class ActionReporter {
    public static void reportVisitor(String qq, String action) {
        new Thread(() -> {
            try {
                String urlString = "https://api.ouom.fun/log.php?qq=" + qq + "&action=" + action;
                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder responseBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                } else {
                    Logger.e("Error: " + status);
                }
                connection.disconnect();
            } catch (Exception e) {
                Logger.e(e);
            }
        }).start();
    }
}
