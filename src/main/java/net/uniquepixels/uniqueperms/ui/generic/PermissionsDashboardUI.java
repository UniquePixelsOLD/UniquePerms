package net.uniquepixels.uniqueperms.ui.generic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.uniquepixels.core.paper.chat.chatinput.ChatInput;
import net.uniquepixels.core.paper.gui.UIRow;
import net.uniquepixels.core.paper.gui.UISlot;
import net.uniquepixels.core.paper.gui.backend.UIHolder;
import net.uniquepixels.core.paper.gui.background.UIBackground;
import net.uniquepixels.core.paper.gui.exception.OutOfInventoryException;
import net.uniquepixels.core.paper.gui.item.UIAction;
import net.uniquepixels.core.paper.gui.item.UIItem;
import net.uniquepixels.core.paper.gui.types.chest.ChestUI;
import net.uniquepixels.core.paper.item.DefaultItemStackBuilder;
import net.uniquepixels.coreapi.MapPaginator;
import net.uniquepixels.uniqueperms.UniquePerms;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import net.uniquepixels.uniqueperms.ui.UiHeads;
import net.uniquepixels.uniqueperms.ui.group.GroupInfoUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PermissionsDashboardUI extends ChestUI {
  private final PermissionManager permissionManager;
  private final UIHolder uiHolder;
  private final UISection section;
  private final UIData data;
  private final HashMap<Integer, HashMap<String, Boolean>> permissions = new HashMap<>();
  private int currentPage;
  private int currentSlot = 0;
  private boolean deletePermission = false;


  public PermissionsDashboardUI(PermissionManager permissionManager, UIHolder uiHolder, UISection section, UIData data, int page) {
    super(Component.translatable("ui.permission.title").color(TextColor.fromHexString("#870ac2")), UIRow.CHEST_ROW_6);
    this.permissionManager = permissionManager;
    this.uiHolder = uiHolder;
    this.section = section;
    this.data = data;
    this.currentPage = page;
  }

  private void loadPermissions() {

    this.permissions.clear();

    switch (section) {
      case GROUP -> {

        if (this.data.group() == null)
          return;

        this.permissions.putAll(new MapPaginator<>(this.data.group().permissions()).maxSizePerPage(45));
      }
      case PLAYER -> {

        if (this.data.player() == null)
          return;

        this.permissions.putAll(new MapPaginator<>(this.data.player().permissions()).maxSizePerPage(45));
      }
    }

  }

  private UIAction handlePermissionClick(String permission, boolean allowed) {
    return (player, uiItem, clickType, inventoryClickEvent) -> {

      if (this.deletePermission) {

        switch (this.section) {
          case PLAYER -> {
            this.data.player().permissions().remove(permission);

            try {
              this.refreshInventory(player);
            } catch (OutOfInventoryException e) {
              throw new RuntimeException(e);
            }
          }
          case GROUP -> {
            this.data.group().permissions().remove(permission);
            try {
              this.refreshInventory(player);
            } catch (OutOfInventoryException e) {
              throw new RuntimeException(e);
            }

          }
        }

        return true;
      }


      switch (this.section) {
        case PLAYER -> {
          this.data.player().permissions().put(permission, !this.data.player().permissions().get(permission));
          try {
            this.refreshInventory(player);
          } catch (OutOfInventoryException e) {
            throw new RuntimeException(e);
          }

        }
        case GROUP -> {
          this.data.group().permissions().put(permission, !this.data.group().permissions().get(permission));
          try {
            this.refreshInventory(player);
          } catch (OutOfInventoryException e) {
            throw new RuntimeException(e);
          }
        }
      }

      return true;
    };
  }

  private void addPermissionItem(String permission, boolean allowed, Player player) throws OutOfInventoryException {

    Locale locale = player.locale();
    Component leftClick = GlobalTranslator.render(Component.translatable("ui.left.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component minus = Component.translatable(" - ").color(NamedTextColor.DARK_GRAY);

    if (this.deletePermission) {
      item(new UIItem(
        new DefaultItemStackBuilder<>(Material.PAPER)
          .addFlags(ItemFlag.values())
          .removeLoreLines()
          .addLoreLine(
            leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.permission.entry.lore.delete"), locale).color(NamedTextColor.RED)))
          )
          .displayName(Component.text(permission.replaceAll("#", ".")).color(NamedTextColor.GREEN))
          .applyItemMeta().buildItem(), UISlot.fromSlotId(this.currentSlot).orElse(UISlot.SLOT_0)), this.handlePermissionClick(permission, allowed));
    } else if (allowed) {
      item(new UIItem(
        new DefaultItemStackBuilder<>(Material.PAPER)
          .addFlags(ItemFlag.values())
          .removeLoreLines()
          .addLoreLine(
            leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.permission.entry.lore.normal")
              .arguments(GlobalTranslator.render(Component.translatable("action.enabled").color(NamedTextColor.GREEN), locale)), locale).color(NamedTextColor.GRAY)))
          )
          .displayName(Component.text(permission.replaceAll("#", ".")).color(NamedTextColor.GREEN))
          .applyItemMeta().buildItem(), UISlot.fromSlotId(this.currentSlot).orElse(UISlot.SLOT_0)), this.handlePermissionClick(permission, allowed));
    } else {
      item(new UIItem(
        new DefaultItemStackBuilder<>(Material.PAPER)
          .addFlags(ItemFlag.values())
          .removeLoreLines()
          .addLoreLine(
            leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.permission.entry.lore.normal")
              .arguments(GlobalTranslator.render(Component.translatable("action.disabled").color(NamedTextColor.RED), locale)), locale).color(NamedTextColor.GRAY)))
          )
          .addEnchantment(Enchantment.MENDING, 1)
          .displayName(Component.text(permission.replaceAll("#", ".")).color(NamedTextColor.RED))
          .applyItemMeta().buildItem(), UISlot.fromSlotId(this.currentSlot).orElse(UISlot.SLOT_0)), this.handlePermissionClick(permission, allowed));

    }

    this.currentSlot++;
  }

  private synchronized void openInventoryAgain(Player player) {
    Bukkit.getScheduler().getMainThreadExecutor(JavaPlugin.getPlugin(UniquePerms.class)).execute(() -> {
      this.uiHolder.open(new PermissionsDashboardUI(this.permissionManager, this.uiHolder, this.section, this.data, this.currentPage), player);
    });
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    Locale locale = player.locale();
    Component prefix = Component.text("UniquePerms").color(TextColor.fromHexString("#870ac2")).append(Component.text(" » ").color(NamedTextColor.GRAY));
    Component uiArrow = Component.text("» ").color(NamedTextColor.GRAY).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component leftClick = GlobalTranslator.render(Component.translatable("ui.left.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component minus = Component.translatable(" - ").color(NamedTextColor.DARK_GRAY);

    this.loadPermissions();

    this.currentSlot = 0;

    if (!this.permissions.isEmpty())
      for (Map.Entry<String, Boolean> entry : this.permissions.get(this.currentPage).entrySet()) {
        String key = entry.getKey();
        Boolean value = entry.getValue();
        addPermissionItem(key, value, player);
      }

    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.PLUS_HEAD.clone())
        .displayName(uiArrow.append(GlobalTranslator.render(Component.translatable("ui.permission.add.title").color(NamedTextColor.GREEN), locale)))
        .removeLoreLines()
        .addLoreLine(
          leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.permission.add.lore"), locale).color(NamedTextColor.GRAY)))
        )
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_45
    ), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.closeInventory();
      player1.sendMessage(prefix.append(Component.translatable("add.permission.message").color(NamedTextColor.GRAY)));

      JavaPlugin.getPlugin(UniquePerms.class).getChatInputManager().addChatInput(new ChatInput(player1, component -> {

        String userInput = this.convertPermission(PlainTextComponentSerializer.plainText().serialize(component));

        switch (this.section) {
          case PLAYER -> this.data.player().permissions().put(userInput, true);
          case GROUP -> this.data.group().permissions().put(userInput, true);
        }

        this.openInventoryAgain(player1);
      }));

      return true;
    });


    DefaultItemStackBuilder<ItemMeta> removeItemStack = new DefaultItemStackBuilder<>(UiHeads.REMOVE_HEAD.clone())
      .displayName(uiArrow.append(GlobalTranslator.render(Component.translatable("ui.permission.remove.title").color(NamedTextColor.RED), locale)))
      .removeLoreLines()
      .addLoreLine(
        leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.permission.remove.lore")
          .arguments(GlobalTranslator.render(Component.translatable("action.disabled").color(NamedTextColor.RED), locale)), locale).color(NamedTextColor.GRAY)))
      );

    if (this.deletePermission)
      removeItemStack = removeItemStack.addEnchantment(Enchantment.MENDING, 1)
        .removeLoreLines()
        .addFlags(ItemFlag.values())
        .addLoreLine(
          leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.permission.remove.lore")
            .arguments(GlobalTranslator.render(Component.translatable("action.enabled").color(NamedTextColor.GREEN), locale)), locale).color(NamedTextColor.GRAY)))
        );

    item(new UIItem(
      removeItemStack.applyItemMeta().buildItem(), UISlot.SLOT_46
    ), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.deletePermission = !this.deletePermission;

      try {
        this.refreshInventory(player1);
      } catch (OutOfInventoryException e) {
        throw new RuntimeException(e);
      }

      return true;
    });

    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.RED_BACK.clone())
        .displayName(uiArrow.append(GlobalTranslator.render(Component.translatable("ui.back"), locale).color(NamedTextColor.RED)))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_47
    ), (player1, uiItem, clickType, inventoryClickEvent) -> {

      switch (section) {
        case GROUP -> this.uiHolder.open(new GroupInfoUI(this.data.group(), this.uiHolder), player1);
        case PLAYER -> player1.sendMessage("Not impl yet!");
      }

      return true;
    });

    if (this.permissions.containsKey(currentPage + 1))
      item(new UIItem(
        new DefaultItemStackBuilder<>(UiHeads.OAK_FORWARD.clone())
          .displayName(Component.text("Nächste Seite"))
          .applyItemMeta()
          .buildItem(), UISlot.SLOT_53
      ), (player1, uiItem, clickType, inventoryClickEvent) -> {

        if (!this.permissions.containsKey(this.currentPage + 1))
          return true;

        this.currentPage++;

        return true;
      });

    if (this.currentPage != 0)
      item(new UIItem(
        new DefaultItemStackBuilder<>(UiHeads.OAK_BACKWARD.clone())
          .displayName(Component.text("Vorherige Seite"))
          .applyItemMeta()
          .buildItem(), UISlot.SLOT_52
      ), (player1, uiItem, clickType, inventoryClickEvent) -> {

        if (this.currentPage == 0)
          return true;

        this.currentPage--;

        return true;
      });

    setBackground(new UIBackground(UIBackground.BackgroundType.BOTTOM_LINE, List.of(
      new UIItem(new DefaultItemStackBuilder<>(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty())
        .addFlags(ItemFlag.values())
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_1)
    )));

  }

  private String convertPermission(String permission) {
    return permission.replaceAll("\\.", "#");
  }

  @Override
  public void onClose(Player player) {

    switch (this.section) {
      case PLAYER -> this.permissionManager.savePlayer(this.data.player());
      case GROUP -> this.permissionManager.saveGroup(this.data.group());
    }

  }

  @Override
  public boolean allowItemMovementInOtherInventories() {
    return false;
  }
}
