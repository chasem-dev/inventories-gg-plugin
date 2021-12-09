package gg.inventories;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(InventoriesMain.getInstance(), () -> InventoriesMain.getInstance().syncPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(InventoriesMain.getInstance(), () -> InventoriesMain.getInstance().syncPlayer(event.getPlayer()));
    }
}
