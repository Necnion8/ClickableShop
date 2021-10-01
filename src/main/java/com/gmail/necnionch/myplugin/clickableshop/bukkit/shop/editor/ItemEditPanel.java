package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.editor;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.ClickableShop;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.ShopEntry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;


public class ItemEditPanel extends Panel {
    private final ShopEditPanel panel;
    private final ShopEntry.Entry entry;

    public ItemEditPanel(ShopEditPanel panel, ShopEntry.Entry entry) {
        super(panel.getPlayer(), 27, "" + ChatColor.GOLD + ChatColor.BOLD + "アイテムの編集");
        this.panel = panel;
        this.entry = entry;
    }

    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[27];

        slots[10] = new PanelItem(entry.getItem()).setClickListener((e, p) -> {
            e.setCursor(entry.getItem().clone());
        }).setItemBuilder((p) -> {
            ItemStack display = entry.getItem().clone();
            ItemMeta meta = display.getItemMeta();
            meta.setLore(Arrays.asList(ClickableShop.formatShopItemDescriptionLines(entry).split("\\n")));
            display.setItemMeta(meta);
            return display;
        });

        slots[12] = PanelItem.createItem(Material.NAME_TAG, ChatColor.DARK_AQUA + "説明文を設定する").setClickListener((e, p) -> {
            startInputDescription(p);
        });
        slots[13] = PanelItem.createItem(Material.EMERALD, ChatColor.DARK_GREEN + "値段を設定する").setClickListener((e, p) -> {
            startInputPrice(p);
        });
        slots[14] = PanelItem.createItem(Material.SHEARS, ChatColor.DARK_RED + "このアイテムを消す").setClickListener((e, p) -> {
            panel.getShop().entries().remove(entry);
            panel.getShop().save();
            panel.open();
        });

        slots[16] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.RED + "ショップ編集に戻る").setClickListener((e, p) -> {
            panel.open();
        });

        return slots;
    }



    private void startInputDescription(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5f, 2f);

        String sample = "&4>>> ここからサンプル\n" + ClickableShop.formatShopItemDescriptionLines(entry) + "\n&4<<< ここまで";

        p.sendMessage(ClickableShop.withPrefix("チャットに説明文を入力して追加します"));
        p.sendMessage(ClickableShop.withPrefix("&a&lTIP: &7「&btext&7」 > &3最後の行に追加"));
        p.sendMessage(ClickableShop.withPrefix("&a&l   : &7「&b-3&7」 > &3で3行目を削除"));
        p.sendMessage(ClickableShop.withPrefix("&a&l   : &7「&b+3 text&7」 > &3で3行目に &2text &3を追加"));
        p.sendMessage(ClickableShop.withPrefix("&a&l   : &7「&b/&7」&3で終了します\n" + sample));

        setMessageHandler((message) -> {
            if (message.startsWith("/")) {
                p.sendMessage(ClickableShop.withPrefix("&a終了"));
                open();
                return true;

            } else {
                ArrayList<String> list = new ArrayList<>(Arrays.asList(entry.getDescriptions()));

                if (message.startsWith("-")) {
                    int idx;
                    try {
                        idx = Integer.parseInt(message.substring(1));
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    try {
                        list.remove(idx-1);
                    } catch (IndexOutOfBoundsException e) {
                        return false;
                    }

                } else if (message.startsWith("+")) {
                    int idx;
                    String line;
                    try {
                        idx = Integer.parseInt(message.substring(1).split(" ", 2)[0]);
                        line = message.split(" ", 2)[1];
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        return false;
                    }

                    try {
                        list.add(idx-1, line);
                    } catch (IndexOutOfBoundsException e) {
                        return false;
                    }

                } else {
                    list.add(message);
                }

                entry.setDescriptions(list.toArray(new String[0]));
                panel.getShop().save();

                String sample1 = "&4>>> ここからサンプル\n" + ClickableShop.formatShopItemDescriptionLines(entry) + "\n&4<<< ここまで";
                p.sendMessage(ClickableShop.withPrefix("&a変更を加えました\n" + sample1));
            }
            return false;
        });
        destroy();
    }

    private void startInputPrice(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5f, 2f);
        p.sendMessage(ClickableShop.withPrefix("チャットに値段を入力してください &7(&f&l/&7を実行して中止)"));
        setMessageHandler((message) -> {
            if (message.startsWith("/")) {
                p.sendMessage(ClickableShop.withPrefix("&6中止しました"));

            } else {
                long price;
                try {
                    price = Long.parseLong(message);
                } catch (NumberFormatException e) {
                    p.sendMessage(ClickableShop.withPrefix("&c正しい数値を入力してください &7(&f&l/&7を実行して中止)"));
                    return false;
                }

                if (0 > price) {
                    p.sendMessage(ClickableShop.withPrefix("&c0以上の数値を入力してください &7(&f&l/&7を実行して中止)"));
                    return false;
                }

                entry.setPrice(price);
                panel.getShop().save();
                p.sendMessage(ClickableShop.withPrefix("&a値段を &e" + price + "&a に変更しました"));
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2f);
            }
            open();
            return true;
        });
        destroy();
    }




}
