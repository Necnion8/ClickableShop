package com.gmail.necnionch.myplugin.clickableshop.bukkit.gui;

import com.gmail.necnionch.myplugin.clickableshop.bukkit.ClickableShop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

public abstract class Panel {
    public static final Set<Panel> PANELS = new HashSet<>();

    private final EventListener listener = new EventListener();
    private final Inventory inventory;
    private final Player player;
    private final Map<ItemStack, PanelItem> cachedItems = new HashMap<>();
    private final ItemStack backgroundItem;
    private MessageHandler messageHandler;
    private MessageListener messageListener;


    public Panel(Player player, int size, String invTitle, ItemStack background) {
        this.inventory = Bukkit.createInventory(null, size, invTitle);
        this.player = player;
        backgroundItem = background;
    }

    public Panel(Player player, int size, String invTitle) {
        this(player, size, invTitle, PanelItem.createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.RESET.toString()).getItemStack());
    }


    public void open() {
        placeItems(build());

        Bukkit.getPluginManager().registerEvents(listener, ClickableShop.getInstance());
        player.openInventory(inventory);
        PANELS.add(this);
    }

    public void destroy(boolean close) {
        HandlerList.unregisterAll(listener);

        if (inventory.equals(player.getOpenInventory().getTopInventory()) && close) {
            player.closeInventory();
        }

        PANELS.remove(this);
    }

    public void destroy() {
        destroy(true);
    }

    public static void destroyAll() {
        new HashSet<>(PANELS).forEach(Panel::destroy);
    }


    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }


    public void placeItems(PanelItem[] items) {
        cachedItems.clear();
        inventory.clear();
        int index = 0;
        for (PanelItem item : items) {
            ItemStack itemStack = null;

            if (item != null)
                itemStack = item.getItemBuilder().build(player);

            if (itemStack != null) {
                cachedItems.put(itemStack, item);
            } else if (backgroundItem != null) {
                itemStack = backgroundItem.clone();
            }

            inventory.setItem(index, itemStack);
            index++;
        }

    }

    private PanelItem selectPanelItem(ItemStack item) {
        return cachedItems.get(item);
    }

    public void setMessageHandler(MessageHandler listener) {
        if (messageListener != null) {
            HandlerList.unregisterAll(messageListener);
        }

        messageHandler = listener;
        if (listener != null) {
            messageListener = new MessageListener();
            Bukkit.getPluginManager().registerEvents(messageListener, ClickableShop.getInstance());
        }
    }



    abstract public PanelItem[] build();

    public boolean onClick(InventoryClickEvent event) {
        return false;
    }



    private class EventListener implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player))
                destroy();
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onClose(InventoryCloseEvent event) {
            if (!inventory.equals(event.getInventory()))
                return;

            destroy(false);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onDrag(InventoryDragEvent event) {
            if (!inventory.equals(event.getInventory()))
                return;

            for (Integer slot : event.getRawSlots()) {
                if (inventory.getSize() > slot) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onClick(InventoryClickEvent event) {
            if (!inventory.equals(event.getInventory()))
                return;

            if (InventoryAction.COLLECT_TO_CURSOR.equals(event.getAction()) && event.getCursor() != null) {
                if (Stream.of(inventory.getContents())
                        .anyMatch(i -> event.getCursor().isSimilar(i))) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }

            if (!inventory.equals(event.getClickedInventory()))
                return;

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            if (!Panel.this.onClick(event)) {
                switch (event.getAction()) {
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
                    case MOVE_TO_OTHER_INVENTORY:
                        break;
                    default:
                        return;
                }

                ItemStack current = event.getCurrentItem();
                PanelItem selected = selectPanelItem(current);

                if (selected != null)
                    selected.getClickListener().click(event, player);
            }

        }

    }

    private class MessageListener implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(getPlayer()))
                setMessageHandler(null);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            if (messageHandler != null && event.getPlayer().equals(getPlayer())) {
                event.setCancelled(true);

                Bukkit.getScheduler().runTask(ClickableShop.getInstance(), () -> {
                    if (messageHandler.onMessage(event.getMessage()))
                        setMessageHandler(null);
                });
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (messageHandler != null && event.getPlayer().equals(getPlayer())) {
                event.setCancelled(true);
                if (!messageHandler.onMessage(event.getMessage()))
                    return;
                setMessageHandler(null);
            }
        }

    }



    public interface MessageHandler {
        boolean onMessage(String message);
    }


}
