package net.uniquepixels.uniqueperms;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.uniquepixels.coreapi.database.MongoDatabase;
import net.uniquepixels.uniqueperms.command.PermissionCommand;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.ResourceBundle;

public class UniquePerms extends JavaPlugin {
  @Override
  public void onEnable() {

    MongoDatabase mongoDatabase = new MongoDatabase("mongodb://mongo-auth:g7iqVbMSTHumk4p9KkK@localhost:27017/?authMechanism=SCRAM-SHA-1");

    TranslationRegistry registry = TranslationRegistry.create(Key.key("unique:perms"));
    ResourceBundle bundle = ResourceBundle.getBundle("translations", Locale.ENGLISH, UTF8ResourceBundleControl.get());
    registry.registerAll(Locale.ENGLISH, bundle, true);

    GlobalTranslator.translator().addSource(registry);

    PermissionManager permissionManager = new PermissionManager(mongoDatabase);

    getCommand("perms").setExecutor(new PermissionCommand(permissionManager));
  }
}
