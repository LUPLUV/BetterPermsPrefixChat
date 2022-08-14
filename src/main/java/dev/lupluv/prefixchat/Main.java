package dev.lupluv.prefixchat;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import dev.lupluv.prefixchat.listener.CloudNetSimpleNameTagsListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main plugin;

    @Override
    public void onEnable() {

        plugin = this;

        Listener listener = new CloudNetSimpleNameTagsListener();

        this.getServer().getPluginManager().registerEvents(listener, this);
        CloudNetDriver.getInstance().getEventManager().registerListener(listener);

    }

    public static Main getPlugin() {
        return plugin;
    }
}
