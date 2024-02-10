package net.uniquepixels.uniqueperms.ui.group;

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
import net.uniquepixels.core.paper.item.skull.SkullItemStackBuilder;
import net.uniquepixels.uniqueperms.UniquePerms;
import net.uniquepixels.uniqueperms.permission.GroupPermission;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import net.uniquepixels.uniqueperms.permission.PermissionStorage;
import net.uniquepixels.uniqueperms.ui.HomeUI;
import net.uniquepixels.uniqueperms.ui.UiHeads;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GroupDashboardUI extends ChestUI {
  private final UIHolder uiHolder;
  private final PermissionStorage storage;
  private final UniquePerms plugin = JavaPlugin.getPlugin(UniquePerms.class);
  private int slotId = 0;

  public GroupDashboardUI(UIHolder uiHolder, PermissionStorage storage) {
    super(Component.translatable("ui.group.dashboard.title").color(TextColor.fromHexString("#870ac2")), UIRow.CHEST_ROW_6);
    this.uiHolder = uiHolder;
    this.storage = storage;
  }

  private synchronized void openInventoryAgain(Player player) {
    Bukkit.getScheduler().getMainThreadExecutor(JavaPlugin.getPlugin(UniquePerms.class)).execute(() -> {
      this.uiHolder.open(new GroupDashboardUI(this.uiHolder, this.storage), player);
    });
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {

    Locale locale = player.locale();
    Component prefix = Component.text("UniquePerms").color(TextColor.fromHexString("#870ac2")).append(Component.text(" » ").color(NamedTextColor.GRAY));
    Component uiArrow = Component.text("»").color(NamedTextColor.GRAY).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component leftClick = GlobalTranslator.render(Component.translatable("ui.left.click"), locale)
      .color(NamedTextColor.BLUE).style(builder -> builder.decoration(TextDecoration.ITALIC, false).build());
    Component minus = Component.translatable(" - ").color(NamedTextColor.DARK_GRAY);

    NamespacedKey itemKey = new NamespacedKey(this.plugin, "group-dashboard-item");

    // Add Group
    item(new UIItem(new SkullItemStackBuilder(UiHeads.PLUS_HEAD.clone())
      .displayName(
        uiArrow.append(Component.space().append(GlobalTranslator.render(Component.translatable("ui.group.dashboard.add.group.title"), locale)
          .color(NamedTextColor.GREEN)))
      )
      .removeLoreLines()
      .addLoreLine(
        leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.group.dashboard.add.group.lore"), locale)
          .color(NamedTextColor.GRAY)))
      )
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_45), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.closeInventory();
      player1.sendMessage(prefix.append(Component.translatable("add.group.message").color(NamedTextColor.GRAY)));
      player1.sendMessage(prefix.append(Component.translatable("add.group.message.optional").color(NamedTextColor.GRAY)));

      this.plugin.getChatInputManager().addChatInput(new ChatInput(player1, component -> {

        String userInput = PlainTextComponentSerializer.plainText().serialize(component);

        Material material;

        Material holding = player1.getInventory().getItemInMainHand().getType();
        if (holding != Material.AIR)
          material = holding;
        else {
          List<Material> list = Arrays.stream(Material.values()).filter(material1 -> material1.isItem() && material1.isEnabledByFeature(player1.getWorld())).toList();
          material = list.get(new Random().nextInt(list.size()));
        }

        if (this.plugin.getPermissionManager().existGroup(userInput)) {
          player1.sendMessage(prefix.append(
            Component.translatable("add.group.exist").color(NamedTextColor.GRAY)
              .arguments(Component.text(userInput).color(NamedTextColor.RED))
          ));

          this.openInventoryAgain(player1);

          return;
        }

        this.plugin.getPermissionManager().saveGroup(new GroupPermission(userInput, new ArrayList<>(), new ArrayList<>(), 50, material));
        player1.sendMessage(prefix.append(
          Component.translatable("add.group.created").color(NamedTextColor.GRAY)
            .arguments(Component.text(userInput).color(NamedTextColor.GREEN))
        ));

        this.openInventoryAgain(player1);
      }));

      return true;
    });
    // Back
    item(new UIItem(new SkullItemStackBuilder(UiHeads.RED_BACK.clone())
      .displayName(uiArrow.append(Component.space().append(GlobalTranslator.render(Component.translatable("ui.back"), locale).color(NamedTextColor.RED))))
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_46), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.uiHolder.open(new HomeUI(this.uiHolder, this.storage), player1);

      return true;
    });

    setBackground(new UIBackground(UIBackground.BackgroundType.BOTTOM_LINE, List.of(
      new UIItem(new DefaultItemStackBuilder<>(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty())
        .addFlags(ItemFlag.values())
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_1)
    )));

    List<GroupPermission> allGroups =  this.storage.getGroupPermissions();

    allGroups.sort(Comparator.comparingInt(GroupPermission::weight).reversed());

    for (GroupPermission groupPermission : allGroups) {
      try {
        item(new UIItem(
            new DefaultItemStackBuilder<>(groupPermission.material())
              .displayName(
                uiArrow.append(Component.space().append(GlobalTranslator.render(Component.translatable("ui.group.dashboard.entry.title")
                    .arguments(Component.text(groupPermission.groupName()).color(TextColor.fromHexString("#870ac2"))), locale)
                  .color(NamedTextColor.GRAY)))
              )
              .addLoreLine(leftClick.append(minus.append(GlobalTranslator.render(Component.translatable("ui.group.dashboard.entry.lore"), locale).color(NamedTextColor.GRAY))))
              .addData(itemKey, PersistentDataType.STRING, groupPermission.groupName())
              .addFlags(ItemFlag.values())
              .applyItemMeta()
              .buildItem(), UISlot.fromSlotId(this.slotId).orElse(UISlot.SLOT_0)),
          (player1, uiItem, clickType, inventoryClickEvent) -> {

            PersistentDataContainer container = uiItem.getItemStack().getItemMeta().getPersistentDataContainer();
            if (!container.has(itemKey))
              return true;

            String groupName = container.get(itemKey, PersistentDataType.STRING);

            if (groupName == null) {
              player1.sendMessage("Groupname is null!");
              return true;
            }

            Optional<GroupPermission> optional = this.storage.getGroupPermission(groupName);

            if (optional.isEmpty())
              return true;

            this.uiHolder.open(new GroupInfoUI(optional.get(), this.storage, this.uiHolder), player1);

            return true;
          });
        this.incrementSlotId();
      } catch (OutOfInventoryException e) {
        throw new RuntimeException(e);
      }
    }

  }

  @Override
  public void onClose(Player player) {

  }

  private void incrementSlotId() {
    this.slotId++;
  }

  @Override
  public boolean allowItemMovementInOtherInventories() {
    return false;
  }
}
