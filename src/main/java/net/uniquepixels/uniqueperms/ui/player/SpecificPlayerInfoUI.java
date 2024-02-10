package net.uniquepixels.uniqueperms.ui.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import net.uniquepixels.uniqueperms.permission.PermissionStorage;
import net.uniquepixels.uniqueperms.permission.PlayerPermission;
import net.uniquepixels.uniqueperms.ui.HomeUI;
import net.uniquepixels.uniqueperms.ui.UiHeads;
import net.uniquepixels.uniqueperms.ui.generic.ExtendFromGroupUI;
import net.uniquepixels.uniqueperms.ui.generic.PermissionsDashboardUI;
import net.uniquepixels.uniqueperms.ui.generic.UIData;
import net.uniquepixels.uniqueperms.ui.generic.UISection;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SpecificPlayerInfoUI extends ChestUI {
  private final OfflinePlayer specificPlayer;
  private final PermissionStorage storage;
  private final UIHolder uiHolder;
  private final Optional<PlayerPermission> optional;

  public SpecificPlayerInfoUI(OfflinePlayer specificPlayer, PermissionStorage storage, UIHolder uiHolder) {
    super(Component.translatable("ui.specific.player.title").color(TextColor.fromHexString("#870ac2")), UIRow.CHEST_ROW_3);
    this.specificPlayer = specificPlayer;
    this.storage = storage;
    this.uiHolder = uiHolder;
    this.optional = this.storage.getPlayerPermission(specificPlayer.getUniqueId());
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    Locale locale = player.locale();
    Component uiArrow = Component.text("» ").color(NamedTextColor.GRAY).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component leftClick = GlobalTranslator.render(Component.translatable("ui.left.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component minus = Component.translatable(" - ").color(NamedTextColor.DARK_GRAY);

    Component prefix = Component.text("UniquePerms").color(TextColor.fromHexString("#870ac2")).append(Component.text(" » ").color(NamedTextColor.GRAY));

    item(new UIItem(
      new SkullItemStackBuilder(Material.PLAYER_HEAD)
        .setSkullOwner(this.specificPlayer)
        .displayName(uiArrow.append(Component.text(this.specificPlayer.getName()).color(NamedTextColor.GRAY)))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_13
    ), (player1, uiItem, clickType, inventoryClickEvent) -> true);

    if (this.optional.isEmpty()) {
      player.closeInventory();
      player.sendMessage(prefix.append(Component.translatable("add.specific.player.unknown").color(NamedTextColor.RED)));
      return;
    }

    PlayerPermission permission = this.optional.get();

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
        .buildItem(), UISlot.SLOT_11), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.onClose(player1);
      this.uiHolder.open(new PermissionsDashboardUI(this.storage, this.uiHolder, UISection.PLAYER, new UIData(null, permission), 0), player1);

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
        .buildItem(), UISlot.SLOT_15), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.uiHolder.open(new ExtendFromGroupUI(this.storage, this.uiHolder, UISection.PLAYER, new UIData(null, permission), 0), player1);

      return true;
    });

    setBackground(new UIBackground(UIBackground.BackgroundType.FULL, List.of(
      new UIItem(new DefaultItemStackBuilder<>(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty())
        .addFlags(ItemFlag.values())
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_1)
    )));

    // Back
    item(new UIItem(new SkullItemStackBuilder(UiHeads.RED_BACK.clone())
      .displayName(uiArrow.append(GlobalTranslator.render(Component.translatable("ui.back"), locale).color(NamedTextColor.RED)))
      .removeLoreLines()
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_18), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.onClose(player1);
      this.uiHolder.open(new HomeUI(this.uiHolder, this.storage), player1);

      return true;
    });
  }

  @Override
  public void onClose(Player player) {

  }

  @Override
  public boolean allowItemMovementInOtherInventories() {
    return false;
  }
}
