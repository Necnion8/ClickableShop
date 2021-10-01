package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.ClickableShop;
import com.gmail.necnionch.myplugin.clickableshop.common.BukkitConfigDriver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;


public class ShopEntry extends BukkitConfigDriver {
    private final String shopId;
    private String displayName;
    private final List<Entry> entries = new ArrayList<>();

    public ShopEntry(String shopId) {
        super(ClickableShop.getInstance(), "shops/" + shopId + ".yml", "shop.yml");
        this.shopId = shopId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Entry> entries() {
        return entries;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getShopId() {
        return shopId;
    }

    public String getDisplayNameOrId() {
        return displayName != null ? displayName : shopId;
    }


    @Override
    public boolean onLoaded(FileConfiguration config) {
        if (super.onLoaded(config)) {
            entries.clear();
            displayName = config.getString("display-name", null);

            for (Map<?, ?> raw : config.getMapList("entries")) {
                if (raw == null || raw.isEmpty())
                    continue;

                try {
                    Map<String, Object> entry = new HashMap<>();
                    for (Map.Entry<?, ?> e : raw.entrySet()) {
                        if (e.getKey() instanceof String && e.getValue() != null)
                            entry.put(((String) e.getKey()), e.getValue());
                    }
                    if (!"buy".equals(entry.get("shop-type")))
                        continue;

                    long price;
                    if (entry.get("shop-price") instanceof Integer) {
                        price = (int) entry.get("shop-price");
                    } else if (entry.get("shop-price") instanceof Long) {
                        price = (long) entry.get("shop-price");
                    } else {
                        continue;
                    }

                    Object rawDescriptions = entry.get("shop-descriptions");
                    List<String> descriptions = new ArrayList<>();
                    if (rawDescriptions instanceof List) {
                        for (Object line : ((List<?>) rawDescriptions)) {
                            descriptions.add(String.valueOf(line));
                        }
                    }

                    entries.add(new Entry(ItemStack.deserialize(entry), price, descriptions.toArray(new String[0])));

                } catch (Throwable e) {
                    getLogger().warning("Failed to serialize (shop: " + shopId + ")");
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }


    public ShopPanel createShopPanel(Player player) {
        return new ShopPanel(player, this);
    }

    @Override
    public boolean save() {
        if (config == null)
            config = new YamlConfiguration();

        config.set("display-name", displayName);
        List<Map<String, Object>> entries = this.entries.stream()
                .map(e -> {
                    Map<String, Object> data = e.item.serialize();
                    data.put("shop-type", "buy");
                    data.put("shop-price", e.price);

                    if (e.descriptions != null && e.descriptions.length > 0)
                        data.put("shop-descriptions", Arrays.asList(e.descriptions));

                    return data;
                })
                .collect(Collectors.toList());
        config.set("entries", entries);
        return super.save();
    }


    public static class Entry {
        private final ItemStack item;
        private long price;
        private String[] descriptions;

        public Entry(ItemStack item, long price, String[] descriptions) {
            this.item = item;
            this.price = price;
            this.descriptions = descriptions;

        }

        public ItemStack getItem() {
            return item;
        }

        public long getPrice() {
            return price;
        }

        public void setPrice(long price) {
            this.price = price;
        }

        public String[] getDescriptions() {
            return descriptions;
        }

        public void setDescriptions(String[] descriptions) {
            this.descriptions = descriptions;
        }

        public ShopItemEntry toShopItem() {
            return new ShopItemEntry(this);
        }


        public String formatDescriptionLines() {
            return ClickableShop.formatShopItemDescriptionLines(this);
        }


    }


}
