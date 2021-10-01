package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.editor;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.ShopEntry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class ItemAddConfirmPanel extends Panel {

    private final ShopEditPanel panel;
    private final ItemStack itemStack;

    public ItemAddConfirmPanel(ShopEditPanel panel, ItemStack itemStack) {
        super(panel.getPlayer(), 36, ChatColor.GOLD + "アイテムを追加しますか？");
        this.panel = panel;
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[36];
        slots[13] = new PanelItem(itemStack);
        slots[20] = PanelItem.createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "キャンセル").setClickListener((e, p) -> {
            getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1f);
            panel.open();
        });
        slots[24] = PanelItem.createItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "追加").setClickListener((e, p) -> {
            getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1f);
            ShopEntry.Entry entry = panel.addItemEntry(itemStack);
            panel.openItemEditor(entry);
        });
        return slots;
    }



}
