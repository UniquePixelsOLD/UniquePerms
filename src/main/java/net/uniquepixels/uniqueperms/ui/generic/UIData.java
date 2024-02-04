package net.uniquepixels.uniqueperms.ui.generic;

import net.uniquepixels.uniqueperms.permission.GroupPermission;
import net.uniquepixels.uniqueperms.permission.PlayerPermission;

public record UIData(GroupPermission group, PlayerPermission player) {
}
