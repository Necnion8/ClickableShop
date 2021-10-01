package com.gmail.necnionch.myplugin.clickableshop.bukkit;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.ShopEntry;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.editor.ShopEditPanel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClickableShopCommand {
    private final Permission PERMS_OPEN = new Permission("clickableshop.command.open");
    private final Permission PERMS_EDIT = new Permission("clickableshop.command.edit");
    private final Permission PERMS_CREATE = new Permission("clickableshop.command.create");
    private final Permission PERMS_DELETE = new Permission("clickableshop.command.delete");
    private final Permission PERMS_SET_NPC = new Permission("clickableshop.command.setnpc");
    private final Permission PERMS_TAB_COMPLETER = new Permission("clickableshop.tabcomplete");

    private final ClickableShop pl = ClickableShop.getInstance();

    public ClickableShopCommand(PluginCommand command, ClickableShopConfig config) {
        command.setExecutor(this::onCommand);
        command.setTabCompleter(this::onComplete);
    }

    private boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("open") && sender.hasPermission(PERMS_OPEN)) {
            String shopId = args[1].toLowerCase();
            Player player;
            try {
                player = Bukkit.getPlayer(args[2]);
                if (player == null) {
                    sender.sendMessage(ClickableShop.withPrefix("&cプレイヤーが見つかりません"));
                    return true;
                }
            } catch (IndexOutOfBoundsException ignored) {
                player = null;
            }

            if (player == null && sender instanceof Player)
                player = ((Player) sender);

            if (player == null) {
                sender.sendMessage(ClickableShop.withPrefix("&cプレイヤーを指定してください"));
                return true;
            }

            ShopEntry shop = pl.getShop(shopId);
            if (shop == null) {
                player.sendMessage(ClickableShop.withPrefix("&cショップ '&e" + shopId + "&c' がありません"));
                if (sender instanceof Player && !player.equals(sender))
                    sender.sendMessage(ClickableShop.withPrefix("&cショップ '&e" + shopId + "&c' がありません"));
                return true;
            }

            shop.createShopPanel(player).open();
            return true;

        } else if (args.length >= 2 && args[0].equalsIgnoreCase("editor") && sender.hasPermission(PERMS_EDIT) && sender instanceof Player) {
            String shopId = args[1].toLowerCase();
            ShopEntry shop = pl.getShop(shopId);

            if (shop == null) {
                sender.sendMessage(ClickableShop.withPrefix("&cそのショップはありません"));
            } else {
                new ShopEditPanel(((Player) sender), shop).open();
            }
            return true;

        } else if (args.length >= 2 && args[0].equalsIgnoreCase("create") && sender.hasPermission(PERMS_CREATE) && sender instanceof Player) {
            String shopId = args[1].toLowerCase();
            if (!ClickableShop.checkShopId(shopId)) {
                sender.sendMessage(ClickableShop.withPrefix("&c半角英数字のみ名前に使えます"));
            } else if (pl.getShop(shopId) != null) {
                sender.sendMessage(ClickableShop.withPrefix("&cそのショップは既に存在します"));
            } else {
                ShopEntry shop = pl.createShop(shopId);
                sender.sendMessage(ClickableShop.withPrefix("&aショップを作成しました"));
                new ShopEditPanel((Player) sender, shop).open();
            }
            return true;

        } else if (args.length >= 2 && args[0].equalsIgnoreCase("delete") && sender.hasPermission(PERMS_DELETE)) {
            String shopId = args[1].toLowerCase();
            ShopEntry shop = pl.getShop(shopId);
            if (shop == null) {
                sender.sendMessage(ClickableShop.withPrefix("&cそのショップはありません"));
            } else {
                pl.deleteShop(shop);
                sender.sendMessage(ClickableShop.withPrefix("&6ショップを削除しました"));
            }
            return true;

        } else if (args.length >= 2 && args[0].equalsIgnoreCase("setnpc") && sender.hasPermission(PERMS_SET_NPC) && sender instanceof Player) {
            String shopId = args[1].toLowerCase();
            if (pl.getShop(shopId) == null) {
                sender.sendMessage(ClickableShop.withPrefix("&cそのショップはありません"));
            } else {
                ((Player) sender).performCommand("npc command add clickshop open " + shopId + " %player%");
            }
            return true;

        }
        return false;
    }

    private List<String> onComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMS_TAB_COMPLETER))
            return Collections.emptyList();

        if (args.length == 1) {
            return generateSuggests(args[0], "editor", "create", "delete", "open", "setnpc");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("editor") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("setnpc"))) {
            return generateSuggests(args[1], Arrays.stream(pl.getShops()).map(ShopEntry::getShopId).toArray(String[]::new));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("open")) {
            return generateSuggests(args[2], Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new));
        }
        return Collections.emptyList();

    }

    private List<String> generateSuggests(String arg, String... params) {
        return Arrays.stream(params)
                .filter(i -> i.toLowerCase().startsWith(arg.toLowerCase()))
                .collect(Collectors.toList());
    }

}
