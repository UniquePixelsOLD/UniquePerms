package net.uniquepixels.uniqueperms.permission;

import com.google.gson.Gson;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public record GroupPermission(String groupName, Map<String, Boolean> permissions, List<String> extendFromGroups, int weight) {

  public static GroupPermission fromDocument(Document document) {
    return new Gson().fromJson(document.toJson(), GroupPermission.class);
  }

  public Document toDocument() {
    return new Gson().fromJson(new Gson().toJson(this), Document.class);
  }

}
