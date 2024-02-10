package net.uniquepixels.uniqueperms.permission;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.uniquepixels.coreapi.database.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PermissionManager {

  private final MongoDatabase mongoDatabase;

  public PermissionManager(MongoDatabase mongoDatabase) {
    this.mongoDatabase = mongoDatabase;
  }

  private MongoCollection<Document> getGroupCollection() {
    return this.mongoDatabase.collection("perms-groups", Document.class);
  }

  private MongoCollection<Document> getPlayerCollection() {
    return this.mongoDatabase.collection("perms-player", Document.class);
  }

  public List<GroupPermission> getAllGroups() {

    List<GroupPermission> list = new ArrayList<>();

    for (Document document : this.getGroupCollection().find()) {
      list.add(GroupPermission.fromDocument(document));
    }

    return list;
  }

  public List<PlayerPermission> getAllPlayers() {

    List<PlayerPermission> list = new ArrayList<>();

    for (Document document : this.getPlayerCollection().find()) {
      list.add(PlayerPermission.fromDocument(document));
    }

    return list;
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

  public void removeGroupByName(String groupName) {
    Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
      this.getGroupCollection().deleteOne(Filters.eq("groupName", groupName));
    });
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

  public boolean existPlayer(String uuid) {
    return this.getPlayerCollection().countDocuments(Filters.eq("uuid", uuid)) > 0L;
  }
}
