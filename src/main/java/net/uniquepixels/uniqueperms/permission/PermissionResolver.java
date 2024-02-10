package net.uniquepixels.uniqueperms.permission;

import java.util.*;

public class PermissionResolver {

  private final PermissionStorage storage;
  private final Set<String> visitedGroups = new HashSet<>();

  public PermissionResolver(PermissionStorage storage) {
    this.storage = storage;
  }

  public List<String> getAllPermissionsForGroup(GroupPermission permission) {
    List<String> allPermissions = new ArrayList<>();
    getAllPermissionsForGroupRecursive(Optional.of(permission), allPermissions);
    return allPermissions;
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private void getAllPermissionsForGroupRecursive(Optional<GroupPermission> optional, List<String> allPermissions) {

    if (optional.isEmpty())
      return;

    GroupPermission permission = optional.get();

    if (this.visitedGroups.contains(permission.groupName()))
      return;

    if (permission.extendFromGroups().isEmpty()) {
      allPermissions.addAll(permission.permissions());

      for (String extendedGroup : permission.extendFromGroups()) {
        getAllPermissionsForGroupRecursive(this.storage.getGroupPermission(extendedGroup), allPermissions);
      }
    }

    this.visitedGroups.add(permission.groupName());
  }
}
