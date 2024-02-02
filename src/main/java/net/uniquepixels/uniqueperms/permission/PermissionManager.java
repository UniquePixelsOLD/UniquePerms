package net.uniquepixels.uniqueperms.permission;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.uniquepixels.coreapi.database.MongoDatabase;
import net.uniquepixels.uniqueperms.UniquePerms;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PermissionManager {

  private final MongoDatabase mongoDatabase;
  private final JavaPlugin plugin;

  public PermissionManager(MongoDatabase mongoDatabase) {
    this.mongoDatabase = mongoDatabase;
    this.plugin = JavaPlugin.getPlugin(UniquePerms.class);
  }

  private MongoCollection<Document> getGroupCollection() {
    return this.mongoDatabase.collection("perms-groups", Document.class);
  }

  private MongoCollection<Document> getPlayerCollection() {
    return this.mongoDatabase.collection("perms-groups", Document.class);
  }

  /**
   * Applies the permissions set for the player into the player
   */
  private void applyPermissionFromPermissionsToPlayer(Player player, Map<String, Boolean> permissions) {
    permissions.forEach((s, aBoolean) -> this.addPermission(player, s, aBoolean));
  }

  /**
   * Applies the permissions from the extended groups on the player and adds the group specific permissions at the end
   */
  private void applyGroupPermissions(Player player, GroupPermission groupPermission) {

    groupPermission.extendFromGroups().stream().map(s -> {
        Optional<Document> join = this.getGroupByName(s).join();
        return join.map(GroupPermission::fromDocument).orElse(null);
      }).filter(Objects::nonNull).sorted((perm1, perm2) -> Math.min(perm1.weight(), perm2.weight()))
      .forEachOrdered(permission -> {
        System.out.println(permission.groupName());
        permission.permissions().forEach((s, aBoolean) -> this.addPermission(player, s, aBoolean));
      });

    groupPermission.permissions().forEach((s, aBoolean) -> this.addPermission(player, s, aBoolean));
  }

  public void updatePlayerPermissions(Player player) {

    CompletableFuture<Optional<Document>> playerById = this.getPlayerById(player.getUniqueId());

    Optional<Document> optional = playerById.join();

    if (optional.isEmpty())
      return;

    PlayerPermission playerPermission = PlayerPermission.fromDocument(optional.get());
    this.applyPermissionFromPermissionsToPlayer(player, playerPermission.permissions());

    playerPermission.groups().stream().map(s -> {
        Optional<Document> join = this.getGroupByName(s).join();
        return join.map(GroupPermission::fromDocument).orElse(null);
      }).filter(Objects::nonNull).sorted((perm1, perm2) -> Math.min(perm1.weight(), perm2.weight()))
      .forEachOrdered(permission -> {
        this.applyGroupPermissions(player, permission);
      });

  }

  private void addPermission(Player player, String permission, boolean allowed) {
    player.addAttachment(this.plugin).setPermission(permission, allowed);
  }

  public void saveGroup(GroupPermission group) {

    if (this.existGroup(group.groupName())) {

      this.getGroupCollection().updateMany(Filters.eq("groupName", group.groupName()), List.of(
        Updates.set("permissions", group.permissions()),
        Updates.set("weight", group.weight()),
        Updates.set("extendFromGroups", group.extendFromGroups())
      ));

      return;
    }

    this.getGroupCollection().insertOne(group.toDocument());
  }

  public boolean existGroup(String groupName) {
    return this.getGroupCollection().countDocuments(Filters.eq("groupName", groupName)) > 0L;
  }

  public CompletableFuture<Optional<Document>> getGroupByName(String groupName) {
    CompletableFuture<Optional<Document>> future = new CompletableFuture<>();
    future.completeAsync(() -> Optional.ofNullable(this.getGroupCollection().find(Filters.eq("groupName", groupName)).first()));
    return future;
  }

  public CompletableFuture<Boolean> removeGroupByName(String groupName) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    future.completeAsync(() -> this.getGroupCollection().deleteOne(Filters.eq("groupName", groupName)).getDeletedCount() > 0L);
    return future;
  }

  public void savePlayer(PlayerPermission player) {

    if (this.existPlayer(player.uuid())) {

      this.getPlayerCollection().updateMany(Filters.eq("uuid", player.uuid()), List.of(
        Updates.set("permissions", player.permissions()),
        Updates.set("groups", player.groups())
      ));

      return;
    }

    this.getPlayerCollection().insertOne(player.toDocument());
  }

  public boolean existPlayer(UUID uid) {
    return this.getGroupCollection().countDocuments(Filters.eq("uuid", uid)) > 0L;
  }

  public CompletableFuture<Optional<Document>> getPlayerById(UUID uuid) {
    CompletableFuture<Optional<Document>> future = new CompletableFuture<>();
    future.completeAsync(() -> Optional.ofNullable(this.getPlayerCollection().find(Filters.eq("uuid", uuid)).first()));
    return future;
  }

  public CompletableFuture<Boolean> removePlayerByName(UUID uuid) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    future.completeAsync(() -> this.getPlayerCollection().deleteOne(Filters.eq("uuid", uuid)).getDeletedCount() > 0L);
    return future;
  }
}
