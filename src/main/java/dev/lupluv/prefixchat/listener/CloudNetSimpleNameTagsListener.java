package dev.lupluv.prefixchat.listener;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionUpdateGroupEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionUpdateUserEvent;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import dev.lupluv.prefixchat.Main;
import dev.lupluv.prefixchat.utils.ScoreboardManager;
import dev.lupluv.prefixchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class CloudNetSimpleNameTagsListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        e.setFormat(ScoreboardManager.getInstance().format(Utils.getPrefix(e.getPlayer())) + e.getPlayer().getName() + " §8: §r"
                + ScoreboardManager.getInstance().format(e.getMessage()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> ScoreboardManager.getInstance().updateNameTags(event.getPlayer()));
    }

    @EventListener
    public void handle(PermissionUpdateUserEvent event) {
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getUniqueId().equals(event.getPermissionUser().getUniqueId()))
                .findFirst()
                .ifPresent(ScoreboardManager.getInstance()::updateNameTags));
    }

    @EventListener
    public void handle(PermissionUpdateGroupEvent event) {
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
            IPermissionUser permissionUser = CloudNetDriver.getInstance().getPermissionManagement()
                    .getUser(player.getUniqueId());

            if (permissionUser != null && permissionUser.inGroup(event.getPermissionGroup().getName())) {
                ScoreboardManager.getInstance().updateNameTags(player);
            }
        }));
    }

}
