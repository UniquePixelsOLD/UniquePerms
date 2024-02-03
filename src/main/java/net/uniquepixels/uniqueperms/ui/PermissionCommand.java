package net.uniquepixels.uniqueperms.ui;

import dev.s7a.base64.Base64ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.uniquepixels.core.paper.gui.backend.UIHolder;
import net.uniquepixels.uniqueperms.UniquePerms;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PermissionCommand implements CommandExecutor {

  private static final Component PREFIX = Component.text("UniquePixels").color(TextColor.fromHexString("#835FE0"))
    .append(Component.space())
    .append(Component.text("»")
      .color(NamedTextColor.GRAY))
    .append(Component.space());

  private final UIHolder uiHolder;
  private final PermissionManager permissionManager;

  public PermissionCommand(UIHolder uiHolder) {
    this.uiHolder = uiHolder;
    this.permissionManager = JavaPlugin.getPlugin(UniquePerms.class).getPermissionManager();
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

    if (!sender.hasPermission("uniqueperms.command.permission") || !(sender instanceof Player player)) {
      sender.sendMessage(PREFIX.append(Component.translatable("command.permissions.needed").color(NamedTextColor.RED)));
      return true;
    }

    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
    if (itemInMainHand.getType() != Material.AIR) {

      String encode = Base64ItemStack.encode(itemInMainHand);

      player.sendMessage(Component.text("COPY CODE")
        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, encode)));

      return true;
    }

    //GroupPermission group1 = new GroupPermission("Test1", new HashMap<>(), List.of(), 100, Material.DIAMOND);
    //GroupPermission group2 = new GroupPermission("Test2", new HashMap<>(), List.of(), 100, Material.DIRT);
    //GroupPermission group3 = new GroupPermission("Test3", new HashMap<>(), List.of(), 100, Material.GRASS_BLOCK);
//
    //this.permissionManager.saveGroup(group1);
    //this.permissionManager.saveGroup(group2);
    //this.permissionManager.saveGroup(group3);

    this.uiHolder.open(new HomeUI(this.uiHolder), player);

    return true;
  }

}
