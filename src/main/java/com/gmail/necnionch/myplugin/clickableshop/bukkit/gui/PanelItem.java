package com.gmail.necnionch.myplugin.clickableshop.bukkit.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PanelItem {

    private ItemStack item;
    private ClickEventListener clickListener = (e, p) -> {};
    private ItemBuilder itemBuilder = (p) -> item;


    public PanelItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public PanelItem setClickListener(ClickEventListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public PanelItem setItemBuilder(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ClickEventListener getClickListener() {
        return clickListener;
    }

    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    public PanelItem clone() {
        PanelItem item = new PanelItem(this.item.clone());
        item.setClickListener(clickListener);
        item.setItemBuilder(itemBuilder);
        return item;
    }



    public interface ClickEventListener {
        void click(InventoryClickEvent event, Player player);
    }

    public interface ItemBuilder {
        ItemStack build(Player player);
    }



    public static PanelItem createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return new PanelItem(item);
    }


}
