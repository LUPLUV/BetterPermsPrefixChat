package dev.lupluv.prefixchat.utils;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import org.bukkit.entity.Player;

public class Utils {

    public static String getPrefix(Player p){
        IPermissionUser user = CloudNetDriver.getInstance().getPermissionManagement().getUser(p.getUniqueId());
        return CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(user).getPrefix();
    }

    public static String getColor(Player p){
        IPermissionUser user = CloudNetDriver.getInstance().getPermissionManagement().getUser(p.getUniqueId());
        return CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(user).getColor();
    }

    public static String getGroup(Player p){
        IPermissionUser user = CloudNetDriver.getInstance().getPermissionManagement().getUser(p.getUniqueId());
        return CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(user).getName();
    }

}
