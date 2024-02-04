package net.uniquepixels.uniqueperms.ui;

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
import net.uniquepixels.uniqueperms.ui.group.GroupDashboardUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Locale;

public class HomeUI extends ChestUI {

  private final UIHolder uiHolder;


  public HomeUI(UIHolder uiHolder) {
    super(Component.translatable("ui.home.title").color(TextColor.fromHexString("#870ac2")), UIRow.CHEST_ROW_3);
    this.uiHolder = uiHolder;
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    Locale locale = player.locale();
    Component uiArrow = Component.text("»").color(NamedTextColor.GRAY).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component leftClick = GlobalTranslator.render(Component.translatable("ui.left.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component minus = Component.translatable(" - ").color(NamedTextColor.DARK_GRAY);

    item(new UIItem(new DefaultItemStackBuilder<>(Material.PLAYER_HEAD)
      .displayName(
        uiArrow.append(
          Component.space().append(GlobalTranslator.render(Component.translatable("ui.home.player.title"), locale).color(TextColor.fromHexString("#870ac2")))
        )
      )
      .addLoreLine(
        leftClick
          .append(
            minus.append(GlobalTranslator.render(Component.translatable("ui.home.player.lore"), locale).color(NamedTextColor.GRAY))
          )
      )
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_14), (player1, uiItem, clickType, event) -> {

      player1.sendMessage("Not implemented yet!");

      return true;
    });

    item(new UIItem(new DefaultItemStackBuilder<>(Material.TURTLE_EGG)
      .displayName(
        uiArrow.append(Component.space().append(
            GlobalTranslator.render(Component.translatable("ui.home.group.title"), locale)
              .color(TextColor.fromHexString("#870ac2"))
          )
        )
      )
      .addLoreLine(
        leftClick.append(minus.append(
            GlobalTranslator.render(Component.translatable("ui.home.group.lore"), locale)
              .color(NamedTextColor.GRAY)
          )
        )
      )
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
