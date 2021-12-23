package gg.inventories.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.inventories.InventoriesCore;
import gg.inventories.adapters.player.PlayerAdapter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpigotPlayerAdapter extends PlayerAdapter<Player, SpigotItemAdapter> {

    static JsonObject airJson = new JsonObject();

    static {
        airJson.addProperty("type", Material.AIR.name());
        airJson.addProperty("unlocalizedName", Material.AIR.getKey().toString());
        airJson.addProperty("source", Material.AIR.getKey().toString().split(":")[0]);
        airJson.addProperty("itemName", Material.AIR.getKey().toString().split(":")[1]);
    }

    @Override
    public JsonObject toJson(Player player) {
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
                inventoryJson.add(this.getItemAdapter().toJson(item));
            } else {
                inventoryJson.add(airJson);
            }
        }
        InventoriesCore.getLogger().fine("Armor of " + player.getName() + " logged.");

        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack item = player.getInventory().getContents()[i];

            if (item != null) {
                inventoryJson.add(this.getItemAdapter().toJson(item));
            } else {
                inventoryJson.add(airJson);
            }
        }
        InventoriesCore.getLogger().fine("Inventory of " + player.getName() + " logged.");

        if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            inventoryJson.add(getItemAdapter().toJson(player.getInventory().getItemInOffHand()));
        } else {
            inventoryJson.add(airJson);
        }
        InventoriesCore.getLogger().fine("Offhand of " + player.getName() + " logged.");

        JsonArray enderInventoryJson = new JsonArray();

        for (int i = 0; i < player.getEnderChest().getContents().length; i++) {
            ItemStack item = player.getEnderChest().getContents()[i];

            if (item != null) {
                enderInventoryJson.add(this.getItemAdapter().toJson(item));
            } else {
                enderInventoryJson.add(airJson);
            }
        }
        InventoriesCore.getLogger().fine("Enderchest of " + player.getName() + " logged.");

        playerInfo.add("inventory", inventoryJson);

        playerInfo.add("enderChest", enderInventoryJson);
        InventoriesCore.getLogger().info("Syncing " + player.getName());
        return playerInfo;
    }

    @Override
    public SpigotItemAdapter getItemAdapter() {
        return new SpigotItemAdapter();
    }
}
