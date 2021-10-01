package com.gmail.necnionch.myplugin.clickableshop.bukkit;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.ShopEntry;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.Panel;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ClickableShop extends JavaPlugin {
    private static ClickableShop instance;
    private final ClickableShopConfig mainConfig = new ClickableShopConfig(this);
    private Economy economy;
    private final Map<String, ShopEntry> shops = new HashMap<>();

    public static ClickableShop getInstance() {
        return Objects.requireNonNull(instance, "Plugin is disabled!");
    }


    @Override
    public void onEnable() {
        instance = this;
        mainConfig.load();

        if (!initEconomy()) {
            getLogger().severe("Failed to get economy service");
            setEnabled(false);
            return;
        }

        PluginCommand command = getCommand("clickshop");
        new ClickableShopCommand(Objects.requireNonNull(command), mainConfig);

        reloadShops();
//        addSampleShop();


//        Player necnion8 = Bukkit.getPlayer("Necnion8");
//        if (necnion8 != null)
//            necnion8.performCommand("cshop");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Panel.destroyAll();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.RED + "Plugin is not enabled!");
        return true;
    }


    public boolean initEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            economy = rsp.getProvider();
        return economy != null;
    }

    public void reloadShops() {
        File root = new File(getDataFolder(), "shops");
        boolean r = root.mkdirs();
        if (!root.isDirectory() || root.listFiles() == null)
            return;

        Pattern pattern = Pattern.compile("^(\\w+)\\.yml$");
        for (File file : Objects.requireNonNull(root.listFiles())) {
            Matcher m = pattern.matcher(file.getName());
            if (!m.matches())
                continue;

            ShopEntry shop = new ShopEntry(m.group(1));
            shops.put(shop.getShopId(), shop);
        }


        if (!shops.isEmpty()) {
            shops.values().forEach(ShopEntry::load);
            getLogger().info("Loaded " + shops.size() + " shops");
        }
    }

    public void addSampleShop() {
        if (shops.isEmpty()) {
            ShopEntry shop = new ShopEntry("sample");
            shop.entries().add(new ShopEntry.Entry(new ItemStack(Material.STONE), 100, new String[0]));
            shops.put(shop.getShopId(), shop);
            shop.save();
        }
    }


    public ShopEntry getShop(String shopId) {
        return shops.get(shopId);
    }

    public ShopEntry[] getShops() {
        return shops.values().toArray(new ShopEntry[0]);
    }

    public ShopEntry createShop(String shopId) {
        if (shops.containsKey(shopId.toLowerCase()))
            throw new IllegalArgumentException("already exists shop id");

        ShopEntry shop = new ShopEntry(shopId.toLowerCase());
        shops.put(shop.getShopId(), shop);
        shop.save();
        return shop;
    }

    public void deleteShop(ShopEntry shop) {
        shops.remove(shop.getShopId(), shop);

        File file = new File(getDataFolder(), "shops/" + shop.getShopId() + ".yml");
        file.delete();
    }



    public static boolean checkShopId(String name) {
        return name.matches("^\\w+");
    }

    public static double getBalance(Player player) {
        return getInstance().economy.getBalance(player);
    }

    public static void withdrawBalance(Player player, double amount) {
        getInstance().economy.withdrawPlayer(player, amount);
    }

    public static void depositBalance(Player player, double amount) {
        getInstance().economy.depositPlayer(player, amount);
    }


    public static String getUnit() {
        return getInstance().mainConfig.getUnit();
    }

    public static String getPrefix() {
        if (instance != null && instance.mainConfig.getPrefix() != null)
            return instance.mainConfig.getPrefix();
        return ChatColor.translateAlternateColorCodes('&', "&7[&aCShop&7] &r");
    }

    public static String withPrefix(String extra) {
        if (instance != null && instance.mainConfig.getPrefix() != null)
            return ChatColor.translateAlternateColorCodes('&', getPrefix() + extra);
        return ChatColor.translateAlternateColorCodes('&', "&7[&aCShop&7] &r" + extra);
    }


    public static String formatShopItemDescriptionLines(ShopEntry.Entry entry, int amount) {
        ClickableShopConfig c = getInstance().mainConfig;
        String lines = c.getDescriptionFormat();
        String descriptions = "&8&o販売情報なし";
        String lore = "&8&oアイテム情報なし";

        if (entry.getDescriptions() != null && entry.getDescriptions().length >= 1)
            descriptions = c.getDescriptionPrefix() + String.join("\n" + c.getDescriptionPrefix(), entry.getDescriptions());

        ItemStack item = entry.getItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.getLore() != null && !meta.getLore().isEmpty())
            lore = String.join("\n", meta.getLore());

        lines = lines.replaceAll("\\{description}", descriptions)
                .replaceAll("\\{price}", String.format("%,d", entry.getPrice()))
                .replaceAll("\\{total_price}", String.format("%,d", entry.getPrice() * Math.max(1, amount)))
                .replaceAll("\\{amount}", String.valueOf(amount))
                .replaceAll("\\{amount_text}", amount > 0 ? String.format("&7[&fx%s&7]", amount) : "")
                .replaceAll("\\{unit}", c.getUnit())
                .replaceAll("\\{lore}", lore);

        return ChatColor.translateAlternateColorCodes('&', lines);
    }

    public static String formatShopItemDescriptionLines(ShopEntry.Entry entry) {
        return formatShopItemDescriptionLines(entry, 0);
    }




}
