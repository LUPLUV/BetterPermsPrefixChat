package dev.lupluv.prefixchat.utils;

import com.google.common.base.Preconditions;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionGroup;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardManager {

    private static ScoreboardManager instance;
    public static ScoreboardManager getInstance() {
        if(instance == null){
            instance = new ScoreboardManager();
        }
        return instance;
    }

    public void updateNameTags(Player player) {
        this.updateNameTags(player, null);
    }

    public void updateNameTags(Player player, Function<Player, IPermissionGroup> playerIPermissionGroupFunction) {
        this.updateNameTags(player, playerIPermissionGroupFunction, null);
    }

    public void updateNameTags(Player player, Function<Player, IPermissionGroup> playerIPermissionGroupFunction,
                               Function<Player, IPermissionGroup> allOtherPlayerPermissionGroupFunction) {
        Preconditions.checkNotNull(player);

        IPermissionUser playerPermissionUser = CloudNetDriver.getInstance().getPermissionManagement()
                .getUser(player.getUniqueId());
        AtomicReference<IPermissionGroup> playerPermissionGroup = new AtomicReference<>(
                playerIPermissionGroupFunction != null ? playerIPermissionGroupFunction.apply(player) : null);

        if (playerPermissionUser != null && playerPermissionGroup.get() == null) {
            playerPermissionGroup
                    .set(CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(playerPermissionUser));

            if (playerPermissionGroup.get() == null) {
                playerPermissionGroup.set(CloudNetDriver.getInstance().getPermissionManagement().getDefaultPermissionGroup());
            }
        }

        int sortIdLength = CloudNetDriver.getInstance().getPermissionManagement().getGroups().stream()
                .map(IPermissionGroup::getSortId)
                .map(String::valueOf)
                .mapToInt(String::length)
                .max()
                .orElse(0);

        this.initScoreboard(player);

        Bukkit.getOnlinePlayers().forEach(all -> {
            this.initScoreboard(all);

            if (playerPermissionGroup.get() != null) {
                this.addTeamEntry(player, all, playerPermissionGroup.get(), sortIdLength);
            }

            IPermissionUser targetPermissionUser = CloudNetDriver.getInstance().getPermissionManagement()
                    .getUser(all.getUniqueId());
            IPermissionGroup targetPermissionGroup =
                    allOtherPlayerPermissionGroupFunction != null ? allOtherPlayerPermissionGroupFunction.apply(all) : null;

            if (targetPermissionUser != null && targetPermissionGroup == null) {
                targetPermissionGroup = CloudNetDriver.getInstance().getPermissionManagement()
                        .getHighestPermissionGroup(targetPermissionUser);

                if (targetPermissionGroup == null) {
                    targetPermissionGroup = CloudNetDriver.getInstance().getPermissionManagement().getDefaultPermissionGroup();
                }
            }

            if (targetPermissionGroup != null) {
                this.addTeamEntry(all, player, targetPermissionGroup, sortIdLength);
            }
        });
    }

    private final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    private void addTeamEntry(Player target, Player all, IPermissionGroup permissionGroup, int highestSortIdLength) {
        int sortIdLength = String.valueOf(permissionGroup.getSortId()).length();
        String teamName = (
                highestSortIdLength == sortIdLength ?
                        permissionGroup.getSortId() :
                        String.format("%0" + highestSortIdLength + "d", permissionGroup.getSortId())
        ) + permissionGroup.getName();

        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        Team team = all.getScoreboard().getTeam(teamName);
        if (team == null) {
            team = all.getScoreboard().registerNewTeam(teamName);
        }

        String prefix = permissionGroup.getPrefix();
        String color = permissionGroup.getColor();
        String suffix = permissionGroup.getSuffix();

        try {
            Method method = team.getClass().getDeclaredMethod("setColor", ChatColor.class);
            method.setAccessible(true);


            if (color != null && !color.isEmpty()) {
                ChatColor chatColor = ChatColor.getByChar(color.replaceAll("&", "").replaceAll("§", ""));
                if (chatColor != null) {
                    method.invoke(team, chatColor);
                }
            } else {
                color = ChatColor.getLastColors(prefix.replace('&', '§'));
                if (!color.isEmpty()) {
                    ChatColor chatColor = ChatColor.getByChar(color.replaceAll("&", "").replaceAll("§", ""));
                    if (chatColor != null) {
                        permissionGroup.setColor(color);
                        CloudNetDriver.getInstance().getPermissionManagement().updateGroup(permissionGroup);
                        method.invoke(team, chatColor);
                    }
                }
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
        }

        team.setPrefix(format(prefix));

        team.setSuffix(format(suffix));

        team.addEntry(target.getName());

        target.setDisplayName(format(permissionGroup.getDisplay() + target.getName()));
    }

    public String format(String s){
        Matcher match = pattern.matcher(s);
        while(match.find()){
            String color = s.substring(match.start(), match.end());
            s = s.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
            match = pattern.matcher(s);
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', s);
    }

    private void initScoreboard(Player all) {
        if (all.getScoreboard().equals(all.getServer().getScoreboardManager().getMainScoreboard())) {
            Scoreboard scoreboard = all.getServer().getScoreboardManager().getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("dev/lupluv/lobby", "dummy");
            all.setScoreboard(scoreboard);
        }
    }

}
