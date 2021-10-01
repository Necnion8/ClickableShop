package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.ClickableShop;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.PanelItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;


public class ShopItemEntry extends PanelItem {

    private final long price;
    private ShopPanel parent;
    private String[] descriptions;
    private ShopEntry.Entry entry;

    public ShopItemEntry(ShopEntry.Entry entry) {
        super(entry.getItem().clone());
        this.entry = entry;
        this.price = entry.getPrice();
        this.descriptions = entry.getDescriptions();

        setClickListener((event, player) -> {
            new BuyConfirmPanel(player, this, parent).open();

        });

        setItemBuilder((p) -> {
            ItemStack item = entry.getItem().clone();
            item.setAmount(1);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList(ClickableShop.formatShopItemDescriptionLines(entry).split("\\n")));
            item.setItemMeta(meta);
            return item;
        });
    }

    public ShopItemEntry clone() {
        ShopItemEntry item = new ShopItemEntry(entry);
        item.setParent(parent);
        return item;
    }

    public long getPrice() {
        return price;
    }


    public ShopPanel getParent() {
        return parent;
    }

    public ShopEntry.Entry getEntry() {
        return entry;
    }

    public void setParent(ShopPanel parent) {
        this.parent = parent;
    }

    public String[] getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String[] descriptions) {
        this.descriptions = descriptions;
    }

}
