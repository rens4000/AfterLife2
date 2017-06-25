package me.boykev.ActionbarAPI;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarAPI
  extends JavaPlugin
  implements Listener
{
  public static Plugin plugin;
  public static boolean works = true;
  public static String nmsver;
  private static boolean useOldMethods = false;
  
  public void onEnable()
  {
    plugin = this;
    getConfig().options().copyDefaults(true);
    saveConfig();
    
    CLUpdate clUpdate = new CLUpdate(this);
    
    Server server = getServer();
    ConsoleCommandSender console = server.getConsoleSender();
    
    nmsver = Bukkit.getServer().getClass().getPackage().getName();
    nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
    if ((nmsver.equalsIgnoreCase("v1_8_R1")) || (nmsver.startsWith("v1_7_"))) {
      useOldMethods = true;
    }
    console.sendMessage(ChatColor.AQUA + getDescription().getName() + " V" + getDescription().getVersion() + " has been enabled!");
    Bukkit.getPluginManager().registerEvents(clUpdate, this);
  }
  
  public static void sendActionBar(Player player, String message)
  {
    if (!player.isOnline()) {
      return;
    }
    ActionBarMessageEvent actionBarMessageEvent = new ActionBarMessageEvent(player, message);
    Bukkit.getPluginManager().callEvent(actionBarMessageEvent);
    if (actionBarMessageEvent.isCancelled()) {
      return;
    }
    if (nmsver.startsWith("v1_12_")) {
      sendActionBarPost112(player, message);
    } else {
      sendActionBarPre112(player, message);
    }
  }
  
  private static void sendActionBarPost112(Player player, String message)
  {
    if (!player.isOnline()) {
      return;
    }
    try
    {
      Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
      Object craftPlayer = craftPlayerClass.cast(player);
      
      Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
      Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
      Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
      Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
      Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + nmsver + ".ChatMessageType");
      Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
      Object chatMessageType = null;
      for (Object obj : chatMessageTypes) {
        if (obj.toString().equals("GAME_INFO")) {
          chatMessageType = obj;
        }
      }
      Object o = c2.getConstructor(new Class[] { String.class }).newInstance(new Object[] { message });
      Object ppoc = c4.getConstructor(new Class[] { c3, chatMessageTypeClass }).newInstance(new Object[] { o, chatMessageType });
      Method m1 = craftPlayerClass.getDeclaredMethod("getHandle", new Class[0]);
      Object h = m1.invoke(craftPlayer, new Object[0]);
      Field f1 = h.getClass().getDeclaredField("playerConnection");
      Object pc = f1.get(h);
      Method m5 = pc.getClass().getDeclaredMethod("sendPacket", new Class[] { c5 });
      m5.invoke(pc, new Object[] { ppoc });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      works = false;
    }
  }
  
  private static void sendActionBarPre112(Player player, String message)
  {
    if (!player.isOnline()) {
      return;
    }
    try
    {
      Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
      Object craftPlayer = craftPlayerClass.cast(player);
      
      Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
      Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
      Object ppoc;
      if (useOldMethods)
      {
        Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
        Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
        Method m3 = c2.getDeclaredMethod("a", new Class[] { String.class });
        Object cbc = c3.cast(m3.invoke(c2, new Object[] { "{\"text\": \"" + message + "\"}" }));
        ppoc = c4.getConstructor(new Class[] { c3, Byte.TYPE }).newInstance(new Object[] { cbc, Byte.valueOf((byte) 2) });
      }
      else
      {
        Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
        Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
        Object o = c2.getConstructor(new Class[] { String.class }).newInstance(new Object[] { message });
        ppoc = c4.getConstructor(new Class[] { c3, Byte.TYPE }).newInstance(new Object[] { o, Byte.valueOf((byte) 2) });
      }
      Method m1 = craftPlayerClass.getDeclaredMethod("getHandle", new Class[0]);
      Object h = m1.invoke(craftPlayer, new Object[0]);
      Field f1 = h.getClass().getDeclaredField("playerConnection");
      Object pc = f1.get(h);
      Method m5 = pc.getClass().getDeclaredMethod("sendPacket", new Class[] { c5 });
      m5.invoke(pc, new Object[] { ppoc });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      works = false;
    }
  }
  
  public static void sendActionBar(Player player, final String message, int duration)
  {
    sendActionBar(player, message);
    if (duration >= 0) {
      new BukkitRunnable()
      {
        private Player val$player;

		public void run()
        {
          ActionBarAPI.sendActionBar(this.val$player, "");
        }
      }
      
        .runTaskLater(plugin, duration + 1);
    }
    while (duration > 40)
    {
      duration -= 40;
      
      new BukkitRunnable()
      {
        private Player val$player;

		public void run()
        {
          ActionBarAPI.sendActionBar(this.val$player, message);
        }
      }
      
        .runTaskLater(plugin, duration);
    }
  }
}
