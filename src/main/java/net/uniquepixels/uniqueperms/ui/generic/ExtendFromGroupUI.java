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
import net.uniquepixels.core.paper.gui.item.UIItem;
import net.uniquepixels.core.paper.gui.types.chest.ChestUI;
import net.uniquepixels.core.paper.item.DefaultItemStackBuilder;
import net.uniquepixels.coreapi.ListPaginator;
import net.uniquepixels.uniqueperms.UniquePerms;
import net.uniquepixels.uniqueperms.permission.GroupPermission;
import net.uniquepixels.uniqueperms.permission.PermissionStorage;
import net.uniquepixels.uniqueperms.ui.UiHeads;
import net.uniquepixels.uniqueperms.ui.group.GroupInfoUI;
import net.uniquepixels.uniqueperms.ui.player.SpecificPlayerInfoUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ExtendFromGroupUI extends ChestUI {
  private final PermissionStorage storage;
  private final UIHolder uiHolder;
  private final UISection section;
  private final UIData data;

  private int currentPage;
  private int currentSlot = 0;
  private List<String> deletingGroups = new ArrayList<>();
  private HashMap<Integer, List<String>> extendFromGroups = new HashMap<>();

  public ExtendFromGroupUI(PermissionStorage storage, UIHolder uiHolder, UISection section, UIData data, int currentPage) {
    super(Component.translatable("ui.extending.groups.title").color(TextColor.fromHexString("#870ac2")), UIRow.CHEST_ROW_6);
    this.storage = storage;
    this.uiHolder = uiHolder;
    this.section = section;
    this.data = data;
    this.currentPage = currentPage;
  }

  private void loadGroups() {

    this.extendFromGroups.clear();

    switch (section) {
      case GROUP -> {

        if (this.data.group() == null)
          return;

        this.extendFromGroups.putAll(new ListPaginator<>(this.data.group().extendFromGroups()).maxSizePerPage(45));
      }
      case PLAYER -> {

        if (this.data.player() == null)
          return;

        this.extendFromGroups.putAll(new ListPaginator<>(this.data.player().groups()).maxSizePerPage(45));
      }
    }

  }

  private synchronized void openInventoryAgain(Player player) {
    Bukkit.getScheduler().getMainThreadExecutor(JavaPlugin.getPlugin(UniquePerms.class)).execute(() -> {
      this.uiHolder.open(new ExtendFromGroupUI(this.storage, this.uiHolder, this.section, this.data, this.currentPage), player);
    });
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    this.loadGroups();
    this.currentSlot = 0;

    Locale locale = player.locale();
    Component prefix = Component.text("UniquePerms").color(TextColor.fromHexString("#870ac2")).append(Component.text(" » ").color(NamedTextColor.GRAY));
    Component uiArrow = Component.text("» ").color(NamedTextColor.GRAY).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component leftClick = GlobalTranslator.render(Component.translatable("ui.left.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component minus = Component.translatable(" - ").color(NamedTextColor.DARK_GRAY);

    if (this.extendFromGroups.containsKey(this.currentPage)) {
      List<String> rawGroups = this.extendFromGroups.get(this.currentPage);

      for (String rawGroup : rawGroups) {

        Optional<GroupPermission> optional = this.storage.getGroupPermission(rawGroup);
        if (optional.isEmpty())
          continue;

        GroupPermission groupPermission = optional.get();

        String groupName = groupPermission.groupName();

        if (this.deletingGroups.contains(groupName))
          item(new UIItem(
            new DefaultItemStackBuilder<>(groupPermission.material())
              .addFlags(ItemFlag.values())
              .removeLoreLines()
              .addEnchantment(Enchantment.MENDING, 1)
              .addLoreLine(
                leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.extending.groups.entry.lore.confirm"), locale).color(NamedTextColor.RED)))
              )
              .displayName(uiArrow.append(Component.text(groupName)))
              .applyItemMeta().buildItem(), UISlot.fromSlotId(this.currentSlot).orElse(UISlot.SLOT_0)
          ), (player1, uiItem, clickType, inventoryClickEvent) -> {

            switch (this.section) {
              case PLAYER -> this.data.player().groups().remove(groupName);
              case GROUP -> this.data.group().extendFromGroups().remove(groupName);
            }

            this.onClose(player1);
            try {
              this.refreshInventory(player1);
            } catch (OutOfInventoryException e) {
              throw new RuntimeException(e);
            }

            return true;
          });

        else
          item(new UIItem(
            new DefaultItemStackBuilder<>(groupPermission.material())
              .addFlags(ItemFlag.values())
              .removeLoreLines()
              .addLoreLine(
                leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.extending.groups.entry.lore.delete"), locale).color(NamedTextColor.RED)))
              )
              .displayName(uiArrow.append(Component.text(groupName)))
              .applyItemMeta().buildItem(), UISlot.fromSlotId(this.currentSlot).orElse(UISlot.SLOT_0)
          ), (player1, uiItem, clickType, inventoryClickEvent) -> {

            this.deletingGroups.add(groupName);

            try {
              this.refreshInventory(player1);
            } catch (OutOfInventoryException e) {
              throw new RuntimeException(e);
            }

            return true;
          });

        this.currentSlot++;
      }

    }

    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.PLUS_HEAD.clone())
        .displayName(uiArrow.append(GlobalTranslator.render(Component.translatable("ui.extending.groups.add.title").color(NamedTextColor.GREEN), locale)))
        .removeLoreLines()
        .addLoreLine(
          leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.extending.groups.add.lore"), locale).color(NamedTextColor.GRAY)))
        )
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_45
    ), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.closeInventory();
      player1.sendMessage(prefix.append(Component.translatable("add.extending.groups.message").color(NamedTextColor.GRAY)));

      JavaPlugin.getPlugin(UniquePerms.class).getChatInputManager().addChatInput(new ChatInput(player1, component -> {

        String userInput = PlainTextComponentSerializer.plainText().serialize(component);

        switch (this.section) {
          case PLAYER -> {
            if (this.data.player().groups().contains(userInput)) {
              player1.sendMessage(prefix.append(Component.translatable("add.extending.groups.message.contains").color(NamedTextColor.GRAY)));
              this.openInventoryAgain(player1);
              return;
            }
            this.data.player().groups().add(userInput);
          }
          case GROUP -> {
            if (this.data.group().extendFromGroups().contains(userInput)) {
              player1.sendMessage(prefix.append(Component.translatable("add.extending.groups.message.contains").color(NamedTextColor.RED)));
              this.openInventoryAgain(player1);
              return;
            }
            this.data.group().extendFromGroups().add(userInput);
          }
        }

        this.onClose(player1);
        this.openInventoryAgain(player1);
      }));

      return true;
    });

    item(new UIItem(
      new DefaultItemStackBuilder<>(UiHeads.RED_BACK.clone())
        .displayName(uiArrow.append(GlobalTranslator.render(Component.translatable("ui.back"), locale).color(NamedTextColor.RED)))
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_47
    ), (player1, uiItem, clickType, inventoryClickEvent) -> {

      switch (section) {
        case GROUP -> this.uiHolder.open(new GroupInfoUI(this.data.group(), this.storage, this.uiHolder), player1);
        case PLAYER -> this.uiHolder.open(new SpecificPlayerInfoUI(Bukkit.getOfflinePlayer(UUID.fromString(this.data.player().uuid())), this.storage, this.uiHolder), player1);
      }

      return true;
    });

    if (this.extendFromGroups.containsKey(currentPage + 1))
      item(new UIItem(
        new DefaultItemStackBuilder<>(UiHeads.OAK_FORWARD.clone())
          .displayName(Component.text("Nächste Seite"))
          .applyItemMeta()
          .buildItem(), UISlot.SLOT_53
      ), (player1, uiItem, clickType, inventoryClickEvent) -> {

        if (!this.extendFromGroups.containsKey(this.currentPage + 1))
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

  @Override
  public void onClose(Player player) {
    switch (this.section) {
      case PLAYER -> this.storage.updatePlayer(this.data.player());
      case GROUP -> this.storage.updateGroup(this.data.group());
    }
  }

  @Override
  public boolean allowItemMovementInOtherInventories() {
    return false;
  }
}
