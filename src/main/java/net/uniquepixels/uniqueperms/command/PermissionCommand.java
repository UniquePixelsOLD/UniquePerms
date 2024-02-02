package net.uniquepixels.uniqueperms.command;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.uniquepixels.uniqueperms.UniquePerms;
import net.uniquepixels.uniqueperms.permission.GroupPermission;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class PermissionCommand implements CommandExecutor, TabCompleter {

  private static final Component PREFIX = Component.text("UniquePixels").color(TextColor.fromHexString("#835FE0"))
    .append(Component.space())
    .append(Component.text("»")
      .color(NamedTextColor.GRAY))
    .append(Component.space());

  private final PermissionManager permissionManager;
  private final JavaPlugin plugin;

  public PermissionCommand(PermissionManager permissionManager) {
    this.permissionManager = permissionManager;
    this.plugin = JavaPlugin.getPlugin(UniquePerms.class);
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

    if (!sender.hasPermission("uniqueperms.command.permission")) {
      sender.sendMessage(PREFIX.append(Component.translatable("command.permissions.needed").color(NamedTextColor.RED)));
      return true;
    }

    if (args.length == 0) {
      this.sendUsage("/perm <player/group>", sender);
      return true;
    }

    switch (args[0].toLowerCase()) {
      case "group" -> {
        if (args.length < 2) {
          this.sendUsage("/perms group <create/delete/add/remove>", sender);
          return true;
        }
        switch (args[1].toLowerCase()) {
          case "create" -> {
            this.sendPerforming(sender);
            this.runAsync(unused -> {

              if (args.length < 4) {
                this.sendUsage("/perm group create <name> <weight>", sender);
                return;
              }

              String groupName = args[2].toLowerCase();

              int weight;

              try {
                weight = Integer.parseInt(args[3]);
              } catch (NumberFormatException e) {
                this.sendUsage("/perm group create <name> <weight> (weight as number)", sender);
                return;
              }

              boolean exist = this.permissionManager.existGroup(groupName);
              if (exist) {
                sender.sendMessage(PREFIX.append(Component.translatable("command.group.exist").color(NamedTextColor.RED)
                  .arguments(Component.text(groupName).color(NamedTextColor.GRAY))));
                return;
              }

              this.permissionManager.saveGroup(new GroupPermission(groupName, new HashMap<>(), new ArrayList<>(), weight));

              this.playSound(sender);
              sender.sendMessage(PREFIX.append(Component.translatable("command.group.create").color(NamedTextColor.GREEN)
                .arguments(Component.text(groupName).color(NamedTextColor.GRAY))));

            });
          }
        }
      }
    }

    return true;
  }

  private void playSound(CommandSender sender) {
    sender.playSound(Sound.sound(Key.key("minecraft:block.note_block.bell"), Sound.Source.MASTER, 30f, 1f));
  }

  private void runAsync(Consumer<Void> consumer) {
    Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
      consumer.accept(null);
    });
  }

  private void sendPerforming(CommandSender sender) {
    sender.sendMessage(PREFIX.append(Component.translatable("command.waiting").color(NamedTextColor.GRAY)));
  }

  private void sendUsage(String usage, CommandSender sender) {
    sender.sendMessage(PREFIX.append(Component.translatable("command.usage").arguments(Component.text(usage).color(NamedTextColor.GOLD))));
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

    List<String> list = new ArrayList<>();

    switch (args.length) {
      case 0 -> {
        list.add("player");
        list.add("group");
      }
      case 1 -> {
        switch (args[0].toLowerCase()) {
          case "group" -> {
            list.add("create");
            list.add("delete");
            list.add("add");
            list.add("remove");
            list.add("extend");
            list.add("graph");
          }
          case "player" -> {
            list.add("add");
            list.add("remove");
            list.add("set");
          }
        }
      }
      case 2 -> {
        switch (args[0].toLowerCase()) {
          case "group" -> {

            switch (args[1].toLowerCase()) {
              case "create", "delete" -> list.add("<name>");
              case "add", "remove" -> list.add("permission");
              case "extend", "graph" -> list.add("<from-group>");
            }

          }
          case "player" -> {
            list.add("add");
            list.add("remove");
            list.add("set");
          }
        }
      }

      case 3 -> {
      }
    }


    List<String> completer = new ArrayList<>();
    String current = args[args.length - 1];
    list.forEach(s -> {
      if (s.startsWith(current))
        completer.add(s);
    });

    return completer;
  }

  /*
   * /perm group [create/delete/set/remove] [permission] [time]
   * /perm user set [group] [time]
   * /perm user [add/remove] [group/permission] [time]
   * [time] => *, [int] (m,h,d,w,M,Y)
   * */

}
