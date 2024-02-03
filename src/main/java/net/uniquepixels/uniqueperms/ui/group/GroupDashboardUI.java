package net.uniquepixels.uniqueperms.ui.group;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import net.uniquepixels.uniqueperms.ui.HomeUI;
import net.uniquepixels.uniqueperms.ui.UiHeads;
import org.bson.Document;
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
  private final UniquePerms plugin = JavaPlugin.getPlugin(UniquePerms.class);
  private int slotId = 0;

  public GroupDashboardUI(UIHolder uiHolder) {
    super(Component.text("Groups"), UIRow.CHEST_ROW_6);
    this.uiHolder = uiHolder;
  }

  @Override
  protected void initItems(Player player) throws OutOfInventoryException {
    NamespacedKey itemKey = new NamespacedKey(this.plugin, "group-dashboard-item");
    // Add Group
    item(new UIItem(new SkullItemStackBuilder(UiHeads.PLUS_HEAD)
      .displayName(Component.text("add group"))
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_45), (player1, uiItem, clickType, inventoryClickEvent) -> {

      player1.closeInventory();
      player1.sendMessage("Schreibe den Group Namen in den Chat");
      player1.sendMessage("Halte ein Item in der Hand, um der Gruppe dieses Item zu geben");

      this.plugin.getChatInputManager().addChatInput(new ChatInput(player1, component -> {

        String userInput = PlainTextComponentSerializer.plainText().serialize(component);

        Material material;

        Material holding = player1.getInventory().getItemInMainHand().getType();
        if (holding != Material.AIR)
          material = holding;
        else {
          List<Material> list = Arrays.stream(Material.values()).filter(material1 -> !material1.isFuel() || !material1.isAir() || !material1.isEmpty()).toList();
          material = list.get(new Random().nextInt(list.size()));
        }

        this.plugin.getPermissionManager().saveGroup(new GroupPermission(userInput, new HashMap<>(), new ArrayList<>(), 50, material));
        player1.sendMessage("Die neue Gruppe " + userInput + " wurde erstellt!");

      }));

      return true;
    });
    // Back
    item(new UIItem(new SkullItemStackBuilder(UiHeads.RED_BACK)
      .displayName(Component.text("Back"))
      .applyItemMeta()
      .buildItem(), UISlot.SLOT_46), (player1, uiItem, clickType, inventoryClickEvent) -> {

      this.uiHolder.open(new HomeUI(this.uiHolder), player1);

      return true;
    });

    setBackground(new UIBackground(UIBackground.BackgroundType.FULL, List.of(
      new UIItem(new DefaultItemStackBuilder<>(Material.GRAY_STAINED_GLASS_PANE)
        .displayName(Component.empty())
        .addFlags(ItemFlag.values())
        .applyItemMeta()
        .buildItem(), UISlot.SLOT_1)
    )));


    PermissionManager permissionManager = this.plugin.getPermissionManager();
    permissionManager.getAllGroups().forEach(groupPermission -> {

      try {
        item(new UIItem(
            new DefaultItemStackBuilder<>(groupPermission.material())
              .displayName(Component.text(groupPermission.groupName()).color(NamedTextColor.DARK_PURPLE))
              .addData(itemKey, PersistentDataType.STRING, groupPermission.groupName())
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

            Optional<Document> optional = permissionManager.getGroupByName(groupName).join();

            if (optional.isEmpty())
              return true;

            this.uiHolder.open(new GroupInfoUI(GroupPermission.fromDocument(optional.get()), this.uiHolder), player1);

            return true;
          });
        this.incrementSlotId();
      } catch (OutOfInventoryException e) {
        throw new RuntimeException(e);
      }
    });

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
