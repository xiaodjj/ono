package moe.ono.util.api.ark;
import static moe.ono.util.Utils.parseURLComponents;
import static moe.ono.util.api.ArkEnv.getAuthAPI;
import static moe.ono.util.api.ArkEnv.getSignatureAPI;

import android.content.Context;

import androidx.annotation.NonNull;


import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import moe.ono.config.ONOConf;
import moe.ono.util.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArkRequest {
    public static String endpoint_getArkCoinsByMid = "coins_query/";

    public static String getArkCoinsByMid(String UID, Context context){
        try {
            URL apiUrl = new URL(getAuthAPI()+endpoint_getArkCoinsByMid + UID + "?s=" + System.currentTimeMillis());
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            reader.close();
            connection.disconnect();
            return result.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void retrieveArkListenerData(String url, Context context, boolean permission, ArkRequestCallback callback) {
        String userCookies = ONOConf.getString("global", "cookies", "no cookies");

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("cookies", userCookies)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(e);
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string();
                        callback.onSuccess(res);
                    } else {
                        if (response.body() != null) {
                            String res = response.body().string();
                            callback.onFailure(new IOException("Unexpected code " + response + "\nresponse: " + res));
                        }

                        callback.onFailure(new IOException("Unexpected code " + response));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                } finally {
                    if (response.body() != null) {
                        response.body().close(); // ensure closing the response body
                    }
                }
            }
        });
    }

    public static void sendSignatureRequest(String endpoint, RequestBody body, Context context, ArkRequestCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String[] url_comp = parseURLComponents(getSignatureAPI());
        String API_signature = String.format("%s://%s", url_comp[1], url_comp[0]);
        Request request;
        if (body != null){
             request = new Request.Builder()
                    .header("Cookie", ONOConf.getString("global", "cookies", ""))
                    .url(API_signature + endpoint)
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .header("Cookie", ONOConf.getString("global", "cookies", ""))
                    .url(API_signature + endpoint)
                    .get()
                    .build();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String res = response.body().string();
                        callback.onSuccess(res);
                    } else {
                        callback.onFailure(new IOException("Unexpected code " + response));
                        assert response.body() != null;
                        Logger.e("response.body().string()", response.body().string());
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }

            }
        });
    }
}
