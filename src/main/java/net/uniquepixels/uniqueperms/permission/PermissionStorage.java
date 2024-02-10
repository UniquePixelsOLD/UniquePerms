package net.uniquepixels.uniqueperms.permission;

import net.kyori.adventure.text.Component;
import net.uniquepixels.uniqueperms.UniquePerms;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

public class PermissionStorage {

  private final UniquePerms plugin = JavaPlugin.getPlugin(UniquePerms.class);
  private final List<PlayerPermission> playerPermissions = new ArrayList<>();
  private final List<GroupPermission> groupPermissions = new ArrayList<>();

  public PermissionStorage() {
    this.schedulePull();
  }

  public List<PlayerPermission> getPlayerPermissions() {
    return playerPermissions;
  }

  public List<GroupPermission> getGroupPermissions() {
    return groupPermissions;
  }

  public void schedulePull() {
    UniquePerms plugin = JavaPlugin.getPlugin(UniquePerms.class);
    this.groupPermissions.clear();
    this.groupPermissions.addAll(plugin.getPermissionManager().getAllGroups());
    this.playerPermissions.clear();
    this.playerPermissions.addAll(plugin.getPermissionManager().getAllPlayers());
  }

  public void removeGroup(String groupName) {
    this.groupPermissions.removeIf(groupPermission -> groupPermission.groupName().equals(groupName));
  }

  public boolean containsGroup(String groupName) {
    return !this.groupPermissions.stream().filter(groupPermission -> groupPermission.groupName().equals(groupName)).toList().isEmpty();
  }

  public boolean containsPlayer(UUID uuid) {
    return !this.playerPermissions.stream().filter(playerPermission -> playerPermission.uuid().equals(uuid.toString())).toList().isEmpty();
  }

  public Optional<GroupPermission> getGroupPermission(String groupName) {
    return this.groupPermissions.stream().filter(groupPermission -> groupPermission.groupName().equals(groupName)).findFirst();
  }

  public Optional<PlayerPermission> getPlayerPermission(UUID uuid) {
    return this.playerPermissions.stream().filter(playerPermission -> playerPermission.uuid().equals(uuid.toString())).findFirst();
  }

  public void updateGroup(GroupPermission groupPermission) {
    this.groupPermissions.add(groupPermission);
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> this.plugin.getPermissionManager().saveGroup(groupPermission));
  }

  public List<String> getAllPermissionsForPlayer(UUID uuid) {
    Optional<PlayerPermission> playerById = this.getPlayerPermission(uuid);

    if (playerById.isEmpty())
      return null;

    PlayerPermission playerPermission = playerById.get();
    PermissionResolver permissionResolver = new PermissionResolver(this);

    List<String> permissions = new ArrayList<>(playerPermission.permissions());

    for (String group : playerPermission.groups()) {
      Optional<GroupPermission> optional1 = this.getGroupPermission(group);

      if (optional1.isEmpty())
        continue;

      GroupPermission groupPermission = optional1.get();
      permissions.addAll(permissionResolver.getAllPermissionsForGroup(groupPermission));
    }


    return permissions;
  }

  public void updatePermissions(Player player) {
    this.craftPermissions(player);
    List<String> permissions = this.getAllPermissionsForPlayer(player.getUniqueId());

    if (permissions == null)
      return;

    permissions.forEach((permission) -> player.addAttachment(this.plugin).setPermission(permission, true));
  }

  private void craftPermissions(Player player) {


    CraftHumanEntity craftPlayer = (CraftHumanEntity) player;

    try {

      Class<CraftHumanEntity> playerClass = CraftHumanEntity.class;

      Field perm = playerClass.getDeclaredField("perm");

      perm.setAccessible(true);
      PermissibleBase base = (PermissibleBase) perm.get(craftPlayer);

      Field opable = PermissibleBase.class.getDeclaredField("opable");
      opable.setAccessible(true);
      ServerOperator operator = (ServerOperator) opable.get(base);
      opable.setAccessible(false);

      perm.set(craftPlayer, new UniquePermissibleBase(operator, this, player.getUniqueId()));

    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void updatePlayer(PlayerPermission playerPermission) {
    this.playerPermissions.add(playerPermission);
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> this.plugin.getPermissionManager().savePlayer(playerPermission));
  }
}
