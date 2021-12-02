package gg.inventories;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gg.inventories.listeners.InventoryListener;
import gg.inventories.util.Locale;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.http.HttpResponse;
import org.bukkit.craftbukkit.libs.org.apache.http.client.HttpClient;
import org.bukkit.craftbukkit.libs.org.apache.http.client.methods.HttpPost;
import org.bukkit.craftbukkit.libs.org.apache.http.entity.StringEntity;
import org.bukkit.craftbukkit.libs.org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;


public final class InventoriesPlugin extends JavaPlugin {

    private static InventoriesPlugin instance;
    private Locale locale;

    public static String API_URL = "http://localhost:3000/api";

    public static InventoriesPlugin getInstance() {
        return instance;
    }

    private String getClientSecret() {
        if (getConfig().contains("clientSecret")) {
            String clientSecret = getConfig().getString("clientSecret");
            if (clientSecret.trim().equals("<INSERT CLIENT SECRET>")) {
                return null;
            }

            return getConfig().getString("clientSecret");
        }
        System.err.println("MISSING CLIENT SECRET...");
        return null;
    }

    @Override
    public void onEnable() {
        instance = this;
        locale = new Locale(this);
        saveDefaultConfig();

        if (getClientSecret() == null) {
            try {
                throw (new Exception("Failed to Start Inventories.gg... Missing clientSecret in config."));
            } catch (Exception exception) {
                exception.printStackTrace();
                onDisable();
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            System.out.println("Client Secret not null: " + getClientSecret());
        }

        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            for (Player onlinePlayer : getServer().getOnlinePlayers()) {
                syncPlayer(onlinePlayer);
            }
        }, 0, 20l * 5);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public void syncPlayer(Player player) {
        System.out.println("Syncing Player: " + player.getName());
        System.out.println("Armor:");

        JsonArray inventoryJson = new JsonArray();
        ArrayList<NBTTagCompound> slots = new ArrayList<>();

        for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
            ItemStack item = player.getInventory().getArmorContents()[i];
            NBTTagCompound info = getItemInfo(item);
            info.setInt("slot", i);
//            playerInfo.set("Armor", playerInfo);
            slots.add(info);
//            System.out.println(info);
        }

        System.out.println("Inventory:");
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack item = player.getInventory().getContents()[i];
            NBTTagCompound info = getItemInfo(item);
            info.setInt("slot", i + 4);
            slots.add(info);
//            playerInfo.set("Inventory", playerInfo);
//            System.out.println(info);
        }

        for (NBTTagCompound slot : slots) {
            inventoryJson.add(itemNbtToJson(slot));
        }

        JsonArray enderInventoryJson = new JsonArray();

        for (int i = 0; i < player.getEnderChest().getContents().length; i++) {
            ItemStack item = player.getEnderChest().getContents()[i];
            NBTTagCompound info = getItemInfo(item);
            info.setInt("slot", i);
            enderInventoryJson.add(itemNbtToJson(info));
        }


        sendUpdateRequest(player.getUniqueId().toString(), player.getName(), inventoryJson, enderInventoryJson);
        System.out.println("Synced.\r\n\r\n");
    }

    public JsonObject itemNbtToJson(NBTTagCompound nbt) {
        JsonObject jsonObject = new JsonObject();
        /**
         * '{amount:1,displayName:"Air",itemName:"air",slot:44,source:"minecraft",unlocalizedName:"minecraft:air"}'
         */
        for (String key : nbt.getKeys()) {
            NBTBase value = nbt.get(key);
            if(value != null) {
                jsonObject.addProperty(key, value.asString());
            }else{
                jsonObject.addProperty(key, "null");
            }
        }

        return  jsonObject;
    }

    public void sendUpdateRequest(String uuid, String username, JsonArray playerInventoryJson, JsonArray endInventoryJson) {


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("UUID", uuid);
        jsonObject.addProperty("Username", username);
        jsonObject.add("Inventory", playerInventoryJson);
        jsonObject.add("EnderInventory", endInventoryJson);

        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            String postUrl = API_URL + "/sync";
            System.out.println(postUrl);
            HttpPost post = new HttpPost(postUrl);
            StringEntity postingString = new StringEntity(jsonObject.toString()); //convert to json
            System.out.println(postingString);
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
            post.setHeader("Authorization", Base64.getEncoder().encodeToString(getClientSecret().getBytes(StandardCharsets.UTF_8)));
            HttpResponse response = httpClient.execute(post);
            System.out.println(response.getStatusLine().getStatusCode());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendHandshake() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name", Bukkit.getServer().getName());
        jsonObject.addProperty("MOTD", Bukkit.getServer().getMotd());
    }

    public NBTTagCompound getItemInfo(ItemStack item) {
        NBTTagCompound finalNBT = new NBTTagCompound();
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }

        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound itemTag = new NBTTagCompound();
        itemStack.save(itemTag);
        String itemId = itemTag.getString("id");

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && !item.getItemMeta().getDisplayName().trim().isEmpty()) {
            finalNBT.setString("displayName", item.getItemMeta().getDisplayName());
        } else {
            String translatedKey = itemId.replace(":", ".");

            if (item.getType().isBlock()) {
                translatedKey = "block." + translatedKey;
            } else {
                translatedKey = "item." + translatedKey;
            }

            String itemName = getLocale().get(translatedKey).trim();
            finalNBT.setString("displayName", itemName);
        }

        finalNBT.setInt("amount", itemTag.getByte("Count"));
        finalNBT.setString("unlocalizedName", itemId);
        finalNBT.setString("source", itemId.split(":")[0]);
        finalNBT.setString("itemName", itemId.split(":")[1]);
//        finalNBT.set("nbt", itemTag);
        return finalNBT;
    }

    public Locale getLocale() {
        return locale;
    }
}
