package gg.inventories;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.inventories.utils.SpigotItemToJson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class InventoriesMain extends JavaPlugin {

    public static InventoriesMain instance;

    public static String apiUrl = "https://inventories-gg.vercel.app/api";

    private JsonObject airJson;

    @Override
    public void onEnable() {
        instance = this;

        this.airJson = new JsonObject();
        this.airJson.addProperty("type", Material.AIR.name());
        this.airJson.addProperty("unlocalizedName", Material.AIR.getKey().toString());
        this.airJson.addProperty("source", Material.AIR.getKey().toString().split(":")[0]);
        this.airJson.addProperty("itemName", Material.AIR.getKey().toString().split(":")[1]);

        this.saveDefaultConfig();

        if (this.getClientSecret() == null) {
            try {
                throw (new Exception("Failed to Start Inventories.gg... Missing clientSecret in config."));
            } catch (Exception exception) {
                exception.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            this.getLogger().warning("Client Secret not null: " + getClientSecret());
        }

        this.getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            for (Player onlinePlayer : this.getServer().getOnlinePlayers()) {
                this.syncPlayer(onlinePlayer);
            }
        }, 0, 20L * 150);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private String getClientSecret() {
        if (this.getConfig().contains("clientSecret")) {
            String clientSecret = this.getConfig().getString("clientSecret");

            if (clientSecret.trim().equals("<INSERT CLIENT SECRET>")) {
                return null;
            }

            return this.getConfig().getString("clientSecret");
        }

        this.getLogger().warning("MISSING CLIENT SECRET...");
        return null;
    }

    public void syncPlayer(Player player) {
        this.getLogger().info("Syncing player " + player.getName() + ".");

        JsonObject playerInfo = new JsonObject();

        playerInfo.addProperty("uuid", player.getUniqueId().toString());
        playerInfo.addProperty("username", player.getName());
        playerInfo.addProperty("displayName", player.getDisplayName());
        playerInfo.addProperty("ping", player.getPing());

        playerInfo.addProperty("level", player.getLevel());
        playerInfo.addProperty("exp", player.getExp());
        playerInfo.addProperty("totalExp", player.getTotalExperience());
        playerInfo.addProperty("expToLevel", player.getExpToLevel());

        playerInfo.addProperty("health", player.getHealth());
        playerInfo.addProperty("hunger", player.getFoodLevel());

        JsonArray inventoryJson = new JsonArray();

        for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
            ItemStack item = player.getInventory().getArmorContents()[i];

            if (item != null && item.getType() != Material.AIR) {
                inventoryJson.add(SpigotItemToJson.toJson(item));
            } else {
                inventoryJson.add(this.airJson);
            }
        }
        this.getLogger().info("Armor of " + player.getName() + " logged.");

        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack item = player.getInventory().getContents()[i];

            if (item != null) {
                inventoryJson.add(SpigotItemToJson.toJson(item));
            } else {
                inventoryJson.add(this.airJson);
            }
        }
        this.getLogger().info("Inventory of " + player.getName() + " logged.");

        if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            inventoryJson.add(SpigotItemToJson.toJson(player.getInventory().getItemInOffHand()));
        } else {
            inventoryJson.add(this.airJson);
        }
        this.getLogger().info("Offhand of " + player.getName() + " logged.");

        JsonArray enderInventoryJson = new JsonArray();

        for (int i = 0; i < player.getEnderChest().getContents().length; i++) {
            ItemStack item = player.getEnderChest().getContents()[i];

            if (item != null) {
                enderInventoryJson.add(SpigotItemToJson.toJson(item));
            } else {
                enderInventoryJson.add(this.airJson);
            }
        }
        this.getLogger().info("Enderchest of " + player.getName() + " logged.");

        playerInfo.add("inventory", inventoryJson);

        playerInfo.add("enderChest", enderInventoryJson);

        this.sendUpdateRequest(playerInfo);
    }

    public void sendUpdateRequest(JsonObject playerData) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            String postUrl = apiUrl + "/sync";

            HttpPost post = new HttpPost(postUrl);
            post.setHeader("Accept-Encoding", "UTF-8");
            post.setHeader("Content-type", "application/json");
            post.setHeader("Authorization", Base64.getEncoder().encodeToString(getClientSecret().getBytes(StandardCharsets.UTF_8)));

            StringEntity postingString = new StringEntity(playerData.toString(), "UTF-8"); //convert to json
            post.setEntity(postingString);

            httpClient.execute(post);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static InventoriesMain getInstance() {
        return instance;
    }
}
