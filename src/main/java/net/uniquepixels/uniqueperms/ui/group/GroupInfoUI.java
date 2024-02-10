package net.uniquepixels.uniqueperms.ui.group;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.uniquepixels.core.paper.gui.UIRow;
import net.uniquepixels.core.paper.gui.UISlot;
import net.uniquepixels.core.paper.gui.backend.UIHolder;
import net.uniquepixels.core.paper.gui.background.UIBackground;
import net.uniquepixels.core.paper.gui.exception.OutOfInventoryException;
import net.uniquepixels.core.paper.gui.item.UIItem;
import net.uniquepixels.core.paper.gui.types.chest.ChestUI;
import net.uniquepixels.core.paper.item.DefaultItemStackBuilder;
import net.uniquepixels.core.paper.item.skull.SkullItemStackBuilder;
import net.uniquepixels.uniqueperms.permission.GroupPermission;
import net.uniquepixels.uniqueperms.permission.PermissionStorage;
import net.uniquepixels.uniqueperms.ui.UiHeads;
import net.uniquepixels.uniqueperms.ui.generic.ExtendFromGroupUI;
import net.uniquepixels.uniqueperms.ui.generic.PermissionsDashboardUI;
import net.uniquepixels.uniqueperms.ui.generic.UIData;
import net.uniquepixels.uniqueperms.ui.generic.UISection;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Locale;

public class GroupInfoUI extends ChestUI {

  private final GroupPermission groupPermission;
  private final PermissionStorage storage;
  private final UIHolder uiHolder;
  private int weight;
  private boolean deleteGroup = false;

  public GroupInfoUI(GroupPermission groupPermission, PermissionStorage storage, UIHolder uiHolder) {
    super(Component.translatable("ui.group.info.title").color(TextColor.fromHexString("#870ac2")), UIRow.CHEST_ROW_6);
    this.groupPermission = groupPermission;
    this.weight = groupPermission.weight();
    this.storage = storage;
    this.uiHolder = uiHolder;
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    Locale locale = player.locale();
    Component uiArrow = Component.text("» ").color(NamedTextColor.GRAY).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component leftClick = GlobalTranslator.render(Component.translatable("ui.left.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component rightClick = GlobalTranslator.render(Component.translatable("ui.right.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component minus = Component.translatable(" - ").color(NamedTextColor.DARK_GRAY);

    // Group Info
    item(new UIItem(
      new DefaultItemStackBuilder<>(this.groupPermission.material())
        .removeLoreLines()
        .displayName(uiArrow.append(
          GlobalTranslator.render(Component.translatable("ui.group.info.group.title").arguments(Component.text(this.groupPermission.groupName())
            .color(TextColor.fromHexString("#870ac2"))), locale).color(NamedTextColor.GRAY)
        ))
        .addFlags(ItemFlag.values())
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_13), (player1, uiItem, clickType, inventoryClickEvent) -> {
      return true;
    });

    // Back
    item(new UIItem(new SkullItemStackBuilder(UiHeads.RED_BACK.clone())
      .displayName(uiArrow.append(GlobalTranslator.render(Component.translatable("ui.back"), locale).color(NamedTextColor.RED)))
      .removeLoreLines()
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_45), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.onClose(player1);
      this.uiHolder.open(new GroupDashboardUI(this.uiHolder, this.storage), player1);

      return true;
    });

    // Permissions Submenu
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.OAK_WOOD_P.clone())
        .removeLoreLines()
        .displayName(
          uiArrow.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.permissions.title"), locale)
              .color(TextColor.fromHexString("#870ac2"))
          )
        )
        .addLoreLine(
          leftClick.append(minus.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.permissions.lore"), locale).color(NamedTextColor.GRAY)
          ))
        )
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_20), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.onClose(player1);
      this.uiHolder.open(new PermissionsDashboardUI(this.storage, this.uiHolder, UISection.GROUP, new UIData(this.groupPermission, null), 0), player1);

      return true;
    });

    // Groups Submenu
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.OAK_WOOD_G.clone())
        .removeLoreLines()
        .displayName(
          uiArrow.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.extend.title"), locale)
              .color(TextColor.fromHexString("#870ac2"))
          )
        )
        .addLoreLine(
          leftClick.append(minus.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.extend.lore"), locale).color(NamedTextColor.GRAY)
          ))
        )
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_38), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.uiHolder.open(new ExtendFromGroupUI(this.storage, this.uiHolder, UISection.GROUP, new UIData(this.groupPermission, null), 0), player1);

      return true;
    });

    // Add weight
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.PLUS_HEAD.clone())
        .removeLoreLines()
        .displayName(
          uiArrow.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.add.title"), locale)
              .color(TextColor.fromHexString("#870ac2"))
          )
        )
        .addLoreLine(
          leftClick.append(minus.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.add.lore.1"), locale).color(NamedTextColor.GRAY)
          ))
        )
        .addLoreLine(
          rightClick.append(minus.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.add.lore.2"), locale).color(NamedTextColor.GRAY)
          ))
        )
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
        .removeLoreLines()
        .displayName(
          uiArrow.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.current.title"), locale)
              .color(TextColor.fromHexString("#870ac2"))
          )
        )
        .addLoreLine(
          GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.current.lore")
              .arguments(Component.text(this.weight).color(TextColor.fromHexString("#870ac2"))), locale)
            .style(Style.style().decoration(TextDecoration.ITALIC, false).build())
            .color(NamedTextColor.GRAY)
        )
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_33), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.sendMessage("current weight");

      return true;
    });

    // Remove weight
    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.REMOVE_HEAD.clone())
        .removeLoreLines()
        .displayName(
          uiArrow.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.remove.title"), locale)
              .color(TextColor.fromHexString("#870ac2"))
          )
        )
        .addLoreLine(
          leftClick.append(minus.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.remove.lore.1"), locale).color(NamedTextColor.GRAY)
          ))
        )
        .addLoreLine(
          rightClick.append(minus.append(
            GlobalTranslator.render(Component.translatable("ui.group.info.group.weight.remove.lore.2"), locale).color(NamedTextColor.GRAY)
          ))
        )
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
          .removeLoreLines()
          .displayName(
            uiArrow.append(
              GlobalTranslator.render(Component.translatable("ui.group.info.group.delete.title"), locale)
                .color(NamedTextColor.RED)
            )
          )
          .addLoreLine(
            leftClick.append(minus.append(
              GlobalTranslator.render(Component.translatable("ui.group.info.group.delete.lore.confirm"), locale).color(NamedTextColor.RED)
            ))
          )
          .addFlags(ItemFlag.values())
          .addEnchantment(Enchantment.MENDING, 1).applyItemMeta()
          .buildItem(), UISlot.SLOT_49), (player1, uiItem, clickType, inventoryClickEvent) -> {

        this.storage.removeGroup(this.groupPermission.groupName());
        this.uiHolder.open(new GroupDashboardUI(this.uiHolder, this.storage), player1);

        return true;
      });

    } else
      item(new UIItem(
        new DefaultItemStackBuilder<>(Material.LAVA_BUCKET)
          .removeLoreLines()
          .displayName(
            uiArrow.append(
              GlobalTranslator.render(Component.translatable("ui.group.info.group.delete.title"), locale)
                .color(NamedTextColor.RED)
            )
          )
          .addLoreLine(
            leftClick.append(minus.append(
              GlobalTranslator.render(Component.translatable("ui.group.info.group.delete.lore.1"), locale).color(NamedTextColor.GRAY)
            ))
          )
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
    this.storage.updateGroup(new GroupPermission(groupPermission.groupName(),
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
