package net.uniquepixels.uniqueperms.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.uniquepixels.core.paper.gui.UIRow;
import net.uniquepixels.core.paper.gui.UISlot;
import net.uniquepixels.core.paper.gui.backend.UIHolder;
import net.uniquepixels.core.paper.gui.background.UIBackground;
import net.uniquepixels.core.paper.gui.exception.OutOfInventoryException;
import net.uniquepixels.core.paper.gui.item.UIItem;
import net.uniquepixels.core.paper.gui.types.chest.ChestUI;
import net.uniquepixels.core.paper.item.DefaultItemStackBuilder;
import net.uniquepixels.uniqueperms.UniquePerms;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import net.uniquepixels.uniqueperms.ui.group.GroupDashboardUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class HomeUI extends ChestUI {

  private final UIHolder uiHolder;
  private final PermissionManager permissionManager = JavaPlugin.getPlugin(UniquePerms.class).getPermissionManager();

  public HomeUI(UIHolder uiHolder) {
    super(Component.text("Home"), UIRow.CHEST_ROW_3);
    this.uiHolder = uiHolder;
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    item(new UIItem(new DefaultItemStackBuilder<>(Material.PLAYER_HEAD)
      .displayName(Component.text("Player").color(NamedTextColor.GRAY))
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_14), (player1, uiItem, clickType, event) -> {

      player1.sendMessage("Not implemented yet!");

      return true;
    });

    item(new UIItem(new DefaultItemStackBuilder<>(Material.TURTLE_EGG)
      .displayName(Component.text("Groups").color(NamedTextColor.GRAY))
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_12), (player1, uiItem, clickType, event) -> {

      this.uiHolder.open(new GroupDashboardUI(this.uiHolder), player1);

      return true;
    });

    this.setBackground(new UIBackground(UIBackground.BackgroundType.FULL, List.of(
      new UIItem(
        new DefaultItemStackBuilder<>(Material.GRAY_STAINED_GLASS_PANE)
          .addFlags(ItemFlag.values())
          .displayName(Component.empty())
          .buildItem(), UISlot.SLOT_0
      )
    )));

  }

  @Override
  public void onClose(Player player) {

  }

  @Override
  public boolean allowItemMovementInOtherInventories() {
    return false;
  }
}
