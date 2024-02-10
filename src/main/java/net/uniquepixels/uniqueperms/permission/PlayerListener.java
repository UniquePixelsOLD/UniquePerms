package net.uniquepixels.uniqueperms.permission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

public class PlayerListener implements Listener {

  private final PermissionStorage permissionStorage;

  public PlayerListener(PermissionStorage permissionStorage) {
    this.permissionStorage = permissionStorage;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {

    Player player = event.getPlayer();
    this.permissionStorage.schedulePull();

    if (!this.permissionStorage.containsPlayer(player.getUniqueId())) {
      this.permissionStorage.updatePlayer(new PlayerPermission(player.getUniqueId().toString(), new ArrayList<>(), new ArrayList<>()));
    }

    this.permissionStorage.updatePermissions(player);


  }

}
