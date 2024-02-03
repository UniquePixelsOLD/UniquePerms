package net.uniquepixels.uniqueperms.ui.group;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.uniquepixels.core.paper.gui.UIRow;
import net.uniquepixels.core.paper.gui.UISlot;
import net.uniquepixels.core.paper.gui.backend.UIHolder;
import net.uniquepixels.core.paper.gui.background.UIBackground;
import net.uniquepixels.core.paper.gui.exception.OutOfInventoryException;
import net.uniquepixels.core.paper.gui.item.UIItem;
import net.uniquepixels.core.paper.gui.types.chest.ChestUI;
import net.uniquepixels.core.paper.item.DefaultItemStackBuilder;
import net.uniquepixels.core.paper.item.skull.SkullItemStackBuilder;
import net.uniquepixels.uniqueperms.UniquePerms;
import net.uniquepixels.uniqueperms.permission.GroupPermission;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import net.uniquepixels.uniqueperms.ui.UiHeads;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GroupInfoUI extends ChestUI {

  private final GroupPermission groupPermission;
  private final PermissionManager permissionManager = JavaPlugin.getPlugin(UniquePerms.class).getPermissionManager();
  private final UIHolder uiHolder;
  private int weight;
  private boolean deleteGroup = false;

  public GroupInfoUI(GroupPermission groupPermission, UIHolder uiHolder) {
    super(Component.text("Group Info"), UIRow.CHEST_ROW_6);
    this.groupPermission = groupPermission;
    this.weight = groupPermission.weight();
    this.uiHolder = uiHolder;
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    // Group Info
    item(new UIItem(
      new DefaultItemStackBuilder<>(this.groupPermission.material())
        .displayName(Component.text(this.groupPermission.groupName()).color(NamedTextColor.GRAY).style(Style.style()
          .decoration(TextDecoration.ITALIC, false).build()))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_13), (player1, uiItem, clickType, inventoryClickEvent) -> {
      return true;
    });

    // Back
    item(new UIItem(new SkullItemStackBuilder(UiHeads.RED_BACK)
      .displayName(Component.text("Back"))
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_45), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.onClose(player1);
      this.uiHolder.open(new GroupDashboardUI(this.uiHolder), player1);

      return true;
    });

    // Permissions Submenu
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.OAK_WOOD_P)
        .displayName(Component.text("Permissions").color(NamedTextColor.GRAY).style(Style.style()
          .decoration(TextDecoration.ITALIC, false).build()))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_20), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.sendMessage("Open Permissions");

      return true;
    });

    // Groups Submenu
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.OAK_WOOD_G)
        .displayName(Component.text("Groups").color(NamedTextColor.GRAY).style(Style.style()
          .decoration(TextDecoration.ITALIC, false).build()))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_38), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.sendMessage("Open extend groups");

      return true;
    });

    // Add weight
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.PLUS_HEAD)
        .displayName(Component.text("Add weight").color(NamedTextColor.GRAY).style(Style.style()
          .decoration(TextDecoration.ITALIC, false).build()))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_24), (player1, uiItem, clickType, inventoryClickEvent) -> {

      if (clickType.isRightClick()) {
        if (this.weight > 90) {
          player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 30f, 1f);
          return true;
        }
        this.weight += 10;
      } else {
        if (this.weight == 100) {
          player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 30f, 1f);
          return true;
        }
        this.weight++;
      }

      try {
        this.refreshInventory(player1);
      } catch (OutOfInventoryException e) {
        throw new RuntimeException(e);
      }

      return true;
    });

    // current weight
    item(new UIItem(
      new DefaultItemStackBuilder<>(Material.CHEST)
        .displayName(Component.text("Current weight").color(NamedTextColor.GRAY).style(Style.style()
          .decoration(TextDecoration.ITALIC, false).build()))
        .addLoreLine(Component.text(weight))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_33), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.sendMessage("current weight");

      return true;
    });

    // Remove weight
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.REMOVE_HEAD)
        .displayName(Component.text("Remove weight").color(NamedTextColor.GRAY).style(Style.style()
          .decoration(TextDecoration.ITALIC, false).build()))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_42), (player1, uiItem, clickType, inventoryClickEvent) -> {

      if (clickType.isRightClick()) {
        if (this.weight < 10) {
          player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 30f, 1f);
          return true;
        }
        this.weight -= 10;
      } else {
        if (this.weight < 1) {
          player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 30f, 1f);
          return true;
        }
        this.weight--;
      }

      try {
        this.refreshInventory(player1);
      } catch (OutOfInventoryException e) {
        throw new RuntimeException(e);
      }

      return true;
    });

    if (deleteGroup) {
      item(new UIItem(
        new DefaultItemStackBuilder<>(Material.LAVA_BUCKET)
          .displayName(Component.text("Delete Group").color(NamedTextColor.GRAY).style(Style.style()
            .decoration(TextDecoration.ITALIC, false).build()))
          .addLoreLine(Component.text("Are you shure sm7b?"))
          .addEnchantment(Enchantment.MENDING, 1).applyItemMeta()
          .buildItem(), UISlot.SLOT_49), (player1, uiItem, clickType, inventoryClickEvent) -> {

        this.permissionManager.removeGroupByName(this.groupPermission.groupName());
        player1.closeInventory();

        return true;
      });

    } else
      item(new UIItem(
        new DefaultItemStackBuilder<>(Material.LAVA_BUCKET)
          .displayName(Component.text("Delete Group").color(NamedTextColor.GRAY).style(Style.style()
            .decoration(TextDecoration.ITALIC, false).build()))
          .applyItemMeta()
          .buildItem(), UISlot.SLOT_49), (player1, uiItem, clickType, inventoryClickEvent) -> {
        this.deleteGroup = true;
          try {
              this.refreshInventory(player1);
          } catch (OutOfInventoryException e) {
              throw new RuntimeException(e);
          }
          return true;
      });



    setBackground(new UIBackground(UIBackground.BackgroundType.FULL, List.of(
      new UIItem(new DefaultItemStackBuilder<>(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty())
        .addFlags(ItemFlag.values())
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_1)
    )));

  }

  @Override
  public void onClose(Player player) {

    this.permissionManager.saveGroup(new GroupPermission(groupPermission.groupName(),
      groupPermission.permissions(),
      groupPermission.extendFromGroups(),
      this.weight,
      groupPermission.material()));

  }

  @Override
  public boolean allowItemMovementInOtherInventories() {
    return false;
  }
}
