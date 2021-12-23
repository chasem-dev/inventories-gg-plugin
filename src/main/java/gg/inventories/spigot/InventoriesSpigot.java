package gg.inventories.spigot;

import gg.inventories.InventoriesCore;
import gg.inventories.adapters.SpigotPlayerAdapter;
import gg.inventories.listeners.InventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public final class InventoriesSpigot extends JavaPlugin {

    private static InventoriesSpigot instance;
    private final SpigotPlayerAdapter playerAdapter = new SpigotPlayerAdapter();

    public static InventoriesSpigot getInstance() {
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
        InventoriesCore.getLogger().warning("MISSING CLIENT SECRET...");
        return null;
    }

    @Override
    public void onEnable() {
        instance = this;
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
            InventoriesCore.setClientSecret(getClientSecret());
        }
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            for (Player onlinePlayer : getServer().getOnlinePlayers()) {
                syncPlayer(onlinePlayer);
            }
        }, 0, 20l * 60 * 2);
    }

    public void syncPlayer(Player player) {
        InventoriesCore.sendUpdateRequest(playerAdapter.toJson(player));
    }
}
