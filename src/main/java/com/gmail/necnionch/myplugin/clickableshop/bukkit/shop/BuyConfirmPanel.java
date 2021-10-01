package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.ClickableShop;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.PanelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BuyConfirmPanel extends Panel {
    private static final int SIZE = 27;
    private static final int[] COUNT_SELECT = new int[] {1, 2, 4, 8, 16, 32, 48, 64};

    private final ShopItemEntry entry;
    private final ShopPanel parent;
    private int selectCountIndex = 0;

    private int amount = 1;


    public BuyConfirmPanel(Player player, ShopItemEntry entry, ShopPanel parent) {
        super(player, SIZE, "" + ChatColor.RED + ChatColor.BOLD + "購入確認");
        this.entry = entry.clone();
        this.parent = parent;
    }

    @Override
    public void open() {
        super.open();
        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5f, 1f);
    }

    public ShopPanel getParent() {
        return parent;
    }



    public PanelItem[] build() {
        long cost = entry.getPrice() * getCurrentAmount();
        long balance = (long) ClickableShop.getBalance(getPlayer());


        PanelItem[] items = new PanelItem[SIZE];

        items[11] = createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "キャンセル").setClickListener((event, player) -> {
            if (event.isShiftClick()) {
                backPanel();
            } else {
                closePanel();
                player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5f, 1f);
            }
        });
        items[13] = entry.setClickListener((event, player) -> {
            if (entry.getItemStack().getType().getMaxStackSize() == 1)
                return;

            if (event.isShiftClick()) {
                int maxCount = Math.toIntExact(balance / entry.getPrice());

                if (0 >= maxCount) {
                    maxCount = 1;
                    selectCountIndex = 0;

                } else {
                    selectCountIndex = -1;
                }

                amount = Math.min(maxCount, 64);

                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f);

            } else if (event.isRightClick()) {
                backCount();
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 2f);
            } else {
                nextCount();
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 2f);
            }
            placeItems(build());

        }).setItemBuilder((p) -> {
            ItemStack item = entry.getItemStack().clone();
            item.setAmount(amount);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList(ClickableShop.formatShopItemDescriptionLines(entry.getEntry(), amount).split("\\n")));
            item.setItemMeta(meta);
            return item;
        });


        if (cost <= balance) {
            String title = ChatColor.translateAlternateColorCodes('&', String.format(
                    "&a%,d%sで購入！", cost, ClickableShop.getUnit()
            ));
            items[15] = createItem(Material.LIME_STAINED_GLASS_PANE, title).setClickListener((event, player) -> {
                if (buyItem()) {
                    if (event.isShiftClick())
                        backPanel();
                    else
                        closePanel();

                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.65f, 0.75f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 2f);
                }
            });
        } else {
            items[15] = createItem(
                    Material.BARRIER,
                    String.format("%s残高が %,d%s 足りません！", ChatColor.RED, cost - balance, ClickableShop.getUnit())
            ).setClickListener((e, p) -> {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 2f);
            });
        }

        return items;
    }


    private static PanelItem createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return new PanelItem(item);
    }


    public int getCurrentAmount() {
        return amount;
    }



    private void nextCount() {
        if (selectCountIndex == -1) {
            int currentCount = amount;

            for (int index = 0; index < COUNT_SELECT.length; index++) {
                int count = COUNT_SELECT[index];
                if (currentCount < count) {
                    selectCountIndex = index;
                    amount = COUNT_SELECT[index];
                    return;
                }
            }
        }

        selectCountIndex++;
        if (selectCountIndex >= COUNT_SELECT.length)
            selectCountIndex = 0;
        amount = COUNT_SELECT[selectCountIndex];
    }

    private void backCount() {
        if (selectCountIndex == -1) {
            int currentCount = amount;

            for (int i = 0; i < COUNT_SELECT.length; i++) {
                int index = COUNT_SELECT.length - 1 - i;
                int count = COUNT_SELECT[index];
                if (currentCount > count) {
                    selectCountIndex = index;
                    amount = COUNT_SELECT[index];
                    return;
                }
            }
        }

        selectCountIndex--;
        if (0 > selectCountIndex)
            selectCountIndex = COUNT_SELECT.length - 1;
        amount = COUNT_SELECT[selectCountIndex];
    }

    private void backPanel() {
        if (parent != null)
            parent.open();
        else
            destroy();
    }

    private boolean buyItem() {
        if (amount * entry.getPrice() > ClickableShop.getBalance(getPlayer()))
            return false;

        ClickableShop.withdrawBalance(getPlayer(), amount * entry.getPrice());
        ItemStack item = entry.getItemStack().clone();
        item.setAmount(amount);
        getPlayer().getInventory().addItem(item);
        return true;
    }

    private void closePanel() {
        destroy();
    }

//    private void refreshItems() {
//        amount = COUNT_SELECT[selectCountIndex];
//    }
//
//    private void refreshItems(int amount) {
//        entry.getItemStack().setAmount(amount);
//        placeItems(build());
//    }


}
