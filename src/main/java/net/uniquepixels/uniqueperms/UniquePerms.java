package net.uniquepixels.uniqueperms;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.uniquepixels.core.paper.chat.chatinput.ChatInputManager;
import net.uniquepixels.core.paper.gui.backend.UIHolder;
import net.uniquepixels.coreapi.database.MongoDatabase;
import net.uniquepixels.uniqueperms.permission.PermissionManager;
import net.uniquepixels.uniqueperms.permission.PermissionStorage;
import net.uniquepixels.uniqueperms.permission.PlayerListener;
import net.uniquepixels.uniqueperms.ui.PermissionCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.ResourceBundle;

public class UniquePerms extends JavaPlugin {

  private PermissionManager permissionManager;
  private PermissionStorage permissionStorage;
  private ChatInputManager chatInputManager;

  public ChatInputManager getChatInputManager() {
    return chatInputManager;
  }

  public PermissionManager getPermissionManager() {
    return permissionManager;
  }

  @Override
  public void onEnable() {

    RegisteredServiceProvider<UIHolder> uiHolderRegisteredServiceProvider = Bukkit.getServicesManager().getRegistration(UIHolder.class);
    RegisteredServiceProvider<ChatInputManager> chatInputManagerRegisteredServiceProvider = Bukkit.getServicesManager().getRegistration(ChatInputManager.class);

    if (uiHolderRegisteredServiceProvider == null || chatInputManagerRegisteredServiceProvider == null)
      return;

    UIHolder uiHolder = uiHolderRegisteredServiceProvider.getProvider();
    this.chatInputManager = chatInputManagerRegisteredServiceProvider.getProvider();


    MongoDatabase mongoDatabase = new MongoDatabase("mongodb://mongo-auth:g7iqVbMSTHumk4p9KkK@localhost:27017/?authMechanism=SCRAM-SHA-1");

    TranslationRegistry registry = TranslationRegistry.create(Key.key("unique:perms"));
    ResourceBundle bundle = ResourceBundle.getBundle("translations", Locale.ENGLISH, UTF8ResourceBundleControl.get());
    registry.registerAll(Locale.ENGLISH, bundle, true);

    GlobalTranslator.translator().addSource(registry);

    this.permissionManager = new PermissionManager(mongoDatabase);
    this.permissionStorage = new PermissionStorage();

    Bukkit.getPluginManager().registerEvents(new PlayerListener(this.permissionStorage), this);

    getCommand("perms").setExecutor(new PermissionCommand(uiHolder, this.permissionStorage));
  }
}
