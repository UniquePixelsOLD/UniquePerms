package net.uniquepixels.uniqueperms.permission;

import com.google.gson.Gson;
import org.bson.Document;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public record GroupPermission(String groupName, Map<String, Boolean> permissions, List<String> extendFromGroups, int weight, Material material) {

  public static GroupPermission fromDocument(Document document) {
    return new Gson().fromJson(document.toJson(), GroupPermission.class);
  }

  public Document toDocument() {
    return new Gson().fromJson(new Gson().toJson(this), Document.class);
  }
}
