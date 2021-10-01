package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.editor;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.ClickableShop;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.ShopEntry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;


public class EditingItemEntry extends PanelItem {

    private final ShopEntry.Entry entry;
    private final ShopEditPanel parent;
    private final ShopEntry shop;

    public EditingItemEntry(ShopEditPanel parent, ShopEntry shop, ShopEntry.Entry entry) {
        super(entry.getItem());
        this.entry = entry;
        this.parent = parent;
        this.shop = shop;

        setClickListener((event, player) -> {
            new ItemEditPanel(parent, entry).open();

        });

        setItemBuilder((p) -> {
            ItemStack item = getItemStack().clone();
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList(ClickableShop.formatShopItemDescriptionLines(entry).split("\\n")));
            item.setItemMeta(meta);
            return item;
        });
    }

    public ShopEditPanel getParent() {
        return parent;
    }

    public ShopEntry.Entry getEntry() {
        return entry;
    }

    public ShopEntry getShop() {
        return shop;
    }

}
