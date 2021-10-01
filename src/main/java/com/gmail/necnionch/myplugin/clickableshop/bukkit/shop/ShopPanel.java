package com.gmail.necnionch.myplugin.clickableshop.bukkit.shop;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.clickableshop.bukkit.gui.PanelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Objects;


public class ShopPanel extends Panel {
    private static final int SIZE = 54;
    private static final int ITEMS = 28;
    private final ShopItemEntry[] items;
    private final ShopEntry shop;
    private int currentPageIndex;


    public ShopPanel(Player player, ShopEntry shop) {
        super(player, SIZE, shop.getDisplayNameOrId(), null);
        this.items = shop.entries().stream()
                .filter(Objects::nonNull)
                .map(ShopEntry.Entry::toShopItem)
                .toArray(ShopItemEntry[]::new);
        this.shop = shop;
    }


    @Override
    public void open() {
        super.open();
        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1f);
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_VILLAGER_WORK_LIBRARIAN, 1f, 1f);
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_VILLAGER_WORK_LIBRARIAN, 1f, 1f);
    }

    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[SIZE];

        PanelItem grand = PanelItem.createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.RESET.toString());

        for (int i = 0; i < 9; i++) {
            slots[i] = grand;
            slots[9*5+i] = grand;
        }

        slots[9  ] = grand;
        slots[9*2] = grand;
        slots[9*3] = grand;
        slots[9*4] = grand;
        slots[9*5] = grand;
        slots[9  +8] = grand;
        slots[9*2+8] = grand;
        slots[9*3+8] = grand;
        slots[9*4+8] = grand;
        slots[9*5+8] = grand;

        int slotOffset = ITEMS * currentPageIndex;

        for (int i = 0; i < ITEMS; i++) {
            int line = i / 7 + 1;
            int row = i % 7;

            if (slotOffset + i >= items.length)
                continue;

            ShopItemEntry shopItem = items[slotOffset + i];
            if (shopItem == null)
                continue;

            shopItem.setParent(this);
            slots[(line * 9 + 1) + row] = shopItem;
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

        slots[9*5+4] = PanelItem.createItem(Material.BARRIER, ChatColor.RED + "閉じる").setClickListener((e, p) -> {
            p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1f);
            destroy();
        });

        return slots;
    }


    public ShopItemEntry[] getItems() {
        return items;
    }

    public ShopEntry getShop() {
        return shop;
    }


    public int getMaxPage() {
        int page = items.length / ITEMS;
        if (items.length % ITEMS >= 1)
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


    public PanelItem createTippedArrow(String name, PotionType type) {
        ItemStack item = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setDisplayName(name);
        meta.setBasePotionData(new PotionData(type));
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return new PanelItem(item);

    }

}
