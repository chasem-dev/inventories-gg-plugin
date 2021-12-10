package gg.inventories.listeners;

import gg.inventories.spigot.InventoriesSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        InventoriesSpigot.getInstance().syncPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        InventoriesSpigot.getInstance().syncPlayer(event.getPlayer());
    }


}
