package net.uniquepixels.uniqueperms.permission;

import com.google.gson.Gson;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlayerPermission(String uuid, List<String> groups, List<String> permissions) {


  public static PlayerPermission fromDocument(Document document) {
    return new Gson().fromJson(document.toJson(), PlayerPermission.class);
  }


  public Document toDocument() {
    return new Gson().fromJson(new Gson().toJson(this), Document.class);
  }

}
