package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.editor;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.ClickableShop;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.PanelItem;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.shop.ShopEntry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Objects;

public class ShopEditPanel extends Panel implements Listener {

    private final ShopEntry shop;
    private static final String TITLE = ChatColor.translateAlternateColorCodes(
            '&', "&6&lエディタ &8- &7[&3%s&7]");

    private static final int SIZE = 54;
    private static final int ITEMS = 28;
    private int currentPageIndex;


    public ShopEditPanel(Player player, ShopEntry shop) {
        super(player, SIZE, String.format(TITLE, shop.getDisplayNameOrId()), null);
        this.shop = shop;
    }


    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[SIZE];
        PanelItem grand = PanelItem.createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.RESET.toString());

        for (int i = 0; i < 18; i++) {
            slots[36 + i] = grand;
        }
        slots[8] = grand;
        slots[9  +8] = grand;
        slots[9*2+8] = grand;
        slots[9*3+8] = grand;
        slots[0] = grand;
        slots[9] = grand;
        slots[9*2] = grand;
        slots[9*3] = grand;

        int slotOffset = ITEMS * currentPageIndex;
        EditingItemEntry[] items = shop.entries().stream()
                .filter(Objects::nonNull)
                .map(e -> new EditingItemEntry(this, shop, e))
                .toArray(EditingItemEntry[]::new);

        for (int i = 0; i < ITEMS; i++) {
            int line = i / 7;
            int row = i % 7;

            if (slotOffset + i >= items.length)
                continue;

            EditingItemEntry item = items[slotOffset + i];
            if (item == null)
                continue;

            slots[(line * 9 + 1) + row] = item;
        }


        String name = "前のページ";
        if (0 < currentPageIndex) {
            slots[9*5+3] = createTippedArrow(ChatColor.AQUA + name, PotionType.WATER_BREATHING).setClickListener((e, p) -> {
                if (backPage())
                    p.playSound(p.getLocation(), Sound.BLOCK_BAMBOO_BREAK, 1f, 2f);
            });
        } else {
            slots[9*5+3] = createTippedArrow("" + ChatColor.GRAY + ChatColor.ITALIC + name, PotionType.TURTLE_MASTER);
        }

        name = "次のページ";
        if (currentPageIndex+1 < getMaxPage()) {
            slots[9*5+5] = createTippedArrow(ChatColor.RED + name, PotionType.INSTANT_HEAL).setClickListener((e, p) -> {
                if (nextPage())
                    p.playSound(p.getLocation(), Sound.BLOCK_BAMBOO_BREAK, 1f, 2f);
            });
        } else {
            slots[9*5+5] = createTippedArrow("" + ChatColor.GRAY + ChatColor.ITALIC + name, PotionType.TURTLE_MASTER);
        }


        slots[9*5+4] = PanelItem.createItem(Material.OAK_DOOR, ChatColor.GOLD + "閉じる").setClickListener((e, p) -> {
            p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 0.5f, 2f);
            destroy();
        });

        slots[9*5+6] = PanelItem.createItem(Material.NAME_TAG, ChatColor.DARK_AQUA + "表示名を変更する").setClickListener((e, p) -> {
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5f, 2f);
            p.sendMessage(ClickableShop.withPrefix("チャットに表示名を入力してください &7(&f&l/&7を実行して中止)"));
            setMessageHandler((message) -> {
                if (message.startsWith("/")) {
                    p.sendMessage(ClickableShop.withPrefix("&6中止しました"));
                    open();

                } else {
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    shop.setDisplayName(message);
                    shop.save();
                    p.sendMessage(ClickableShop.withPrefix("&a表示名を &e" + message + "&a に変更しました"));
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2f);

                    ShopEditPanel shop = new ShopEditPanel(getPlayer(), this.shop);
                    shop.currentPageIndex = this.currentPageIndex;
                    shop.open();
                }
                return true;
            });
            destroy();
        });



        return slots;
    }




    public int getMaxPage() {
        int entrySize = shop.entries().size();
        int page = entrySize / ITEMS;
        if (entrySize % ITEMS >= 1)
            page++;
        return page;
    }

    public boolean nextPage() {
        if (getMaxPage() <= currentPageIndex + 1)
            return false;

        currentPageIndex++;
        placeItems(build());
        return true;
    }

    public boolean backPage() {
        if (0 >= currentPageIndex)
            return false;

        currentPageIndex--;
        placeItems(build());
        return true;
    }


    public void openItemEditor(ShopEntry.Entry entry) {
        if (!shop.entries().contains(entry))
            throw new IllegalArgumentException("entry is not contains this shop");

        new ItemEditPanel(this, entry).open();
    }

    public ShopEntry.Entry addItemEntry(ItemStack item) {
        ShopEntry.Entry entry = new ShopEntry.Entry(item, 1000, new String[0]);
        shop.entries().add(entry);
        shop.save();
        return entry;
    }


    public PanelItem createTippedArrow(String name, PotionType type) {
        ItemStack item = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setDisplayName(name);
        meta.setBasePotionData(new PotionData(type));
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return new PanelItem(item);

    }


    public ShopEntry getShop() {
        return shop;
    }


    @Override
    public boolean onClick(InventoryClickEvent event) {
        switch (event.getAction()) {
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
                break;
            default:
                return false;
        }

        ItemStack item = event.getCursor();
        if (item == null)
            return false;

        event.setCurrentItem(null);
        event.setCursor(null);

        boolean newItem = true;
        for (ShopEntry.Entry e : shop.entries()) {
            if (item.isSimilar(e.getItem())) {
                openItemEditor(e);
                newItem = false;
                break;
            }
        }

        if (newItem)
            new ItemAddConfirmPanel(this, item.clone()).open();

        event.getWhoClicked().getInventory().addItem(item);

        return true;
    }


}
