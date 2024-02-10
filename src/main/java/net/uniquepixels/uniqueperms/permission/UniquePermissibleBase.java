package net.uniquepixels.uniqueperms.permission;

import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class UniquePermissibleBase extends PermissibleBase {
  private final PermissionStorage permissionStorage;
  private final UUID uuid;

  public UniquePermissibleBase(@Nullable ServerOperator opable, PermissionStorage permissionStorage, UUID uuid) {
    super(opable);
    this.permissionStorage = permissionStorage;
    this.uuid = uuid;
  }

  @Override
  public boolean hasPermission(@NotNull Permission perm) {
    return this.hasPermission(perm.getName());
  }

  @Override
  public boolean isPermissionSet(@NotNull Permission perm) {
    return this.isPermissionSet(perm.getName());
  }

  @Override
  public boolean isPermissionSet(@NotNull String name) {
    return this.hasPermission(name);
  }

  @Override
  public boolean hasPermission(@NotNull String permission) {
    List<String> permissions = permissionStorage.getAllPermissionsForPlayer(uuid);

    if (permissions.contains("*"))
      return true;

    // if the permission is directly set
    if (permissions.contains(permission))
      return true;

    for (String perm : permissions) {
      if (perm.endsWith("*") && permission.startsWith(perm.substring(0, perm.length() - 1))) {
        return true;
      }
    }

    return false;
  }
}
