package com.gmail.necnionch.myplugin.clickableshop.bukkit;

import com.gmail.necnionch.myplugin.clickableshop.common.BukkitConfigDriver;
import org.bukkit.plugin.java.JavaPlugin;

public class ClickableShopConfig extends BukkitConfigDriver {
    public ClickableShopConfig(JavaPlugin plugin) {
        super(plugin);
    }


    public String getPrefix() {
        return config.getString("prefix", null);
    }

    public String getUnit() {
        return config.getString("unit", null);
    }

    public String getDescriptionFormat() {
        return config.getString("description-format", "");
    }

    public String getDescriptionPrefix() {
        return config.getString("description-prefix", "");
    }

}
