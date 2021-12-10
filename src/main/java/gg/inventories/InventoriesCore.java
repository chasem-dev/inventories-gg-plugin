package gg.inventories;

import com.google.gson.JsonObject;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class InventoriesCore {
    private static String CLIENT_SECRET = null;
    //        public static String API_URL = "http://localhost:3000/api";
    public static String API_URL = "https://inventories-gg.vercel.app/api";

    public static Logger getLogger() {
        return Logger.getLogger("InventoriesCore");
    }

    public static void setClientSecret(String clientSecret) {
        CLIENT_SECRET = clientSecret;
    }

    public static void sendUpdateRequest(JsonObject playerData) {
        if (CLIENT_SECRET == null) {
            getLogger().warning("Failed to send Inventory Update Request, no Client Secret provided.");
            return;
        }
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            String postUrl = API_URL + "/sync";
            HttpPost post = new HttpPost(postUrl);
            post.setHeader("Accept-Encoding", "UTF-8");
            post.setHeader("Content-type", "application/json");
            post.setHeader("Authorization", Base64.getEncoder().encodeToString(CLIENT_SECRET.getBytes(StandardCharsets.UTF_8)));
            StringEntity postingString = new StringEntity(playerData.toString(), "UTF-8"); //convert to json
            post.setEntity(postingString);
            httpClient.execute(post);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

