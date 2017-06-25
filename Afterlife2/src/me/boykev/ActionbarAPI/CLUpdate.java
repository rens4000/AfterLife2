package me.boykev.ActionbarAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CLUpdate
  implements Listener
{
  private UpdateResult result = UpdateResult.DISABLED;
  private String version;
  private Plugin plugin;
  private String message = null;
  private String pluginMessage = null;
  private String updateMessage = null;
  private boolean updateAvailable = false;
  
  public static enum UpdateResult
  {
    NO_UPDATE,  DISABLED,  UPDATE_AVAILABLE;
    
    private UpdateResult() {}
  }
  
  public CLUpdate(JavaPlugin plugin)
  {
    this.plugin = plugin;
    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
    {
      public void run()
      {
        CLUpdate.this.doCheck();
      }
    });
  }
  
  private void doCheck()
  {
    String data = null;
    String url = "http://api.connorlinfoot.com/v1/resource/release/" + this.plugin.getDescription().getName().toLowerCase() + "/";
    try
    {
      data = doCurl(url);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    JSONParser jsonParser = new JSONParser();
    try
    {
      JSONObject obj = (JSONObject)jsonParser.parse(data);
      if (obj.get("version") != null)
      {
        String newestVersion = (String)obj.get("version");
        String currentVersion = this.plugin.getDescription().getVersion().replaceAll("-SNAPSHOT-", ".");
        if (Integer.parseInt(newestVersion.replace(".", "")) > Integer.parseInt(currentVersion.replace(".", "")))
        {
          this.result = UpdateResult.UPDATE_AVAILABLE;
          this.version = ((String)obj.get("version"));
        }
        else
        {
          this.result = UpdateResult.NO_UPDATE;
        }
        if (obj.containsKey("message"))
        {
          this.message = ChatColor.translateAlternateColorCodes('&', (String)obj.get("message"));
          Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', (String)obj.get("message")));
        }
      }
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    Bukkit.getScheduler().runTask(this.plugin, new Runnable()
    {
      public void run()
      {
        CLUpdate.this.handleResult();
      }
    });
  }
  
  public String getVersion()
  {
    return this.version;
  }
  
  public String doCurl(String urlString)
    throws IOException
  {
    URL url = new URL(urlString);
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("POST");
    con.setInstanceFollowRedirects(true);
    con.setDoOutput(true);
    con.setDoInput(true);
    DataOutputStream output = new DataOutputStream(con.getOutputStream());
    output.close();
    DataInputStream input = new DataInputStream(con.getInputStream());
    
    StringBuilder resultBuf = new StringBuilder();
    int c;
    while ((c = input.read()) != -1) {
      resultBuf.append((char)c);
    }
    input.close();
    return resultBuf.toString();
  }
  
  public String getMessage()
  {
    return this.message;
  }
  
  public void handleResult()
  {
    if (getMessage() != null) {
      this.pluginMessage = getMessage();
    }
    switch (this.result)
    {
    case NO_UPDATE: 
    default: 
      this.updateAvailable = false;
      this.updateMessage = "No update was found, you are running the latest version.";
      break;
    case DISABLED: 
      this.updateAvailable = false;
      this.updateMessage = "You currently have update checks disabled";
      break;
    case UPDATE_AVAILABLE: 
      this.updateAvailable = true;
      this.updateMessage = ("An update for " + this.plugin.getDescription().getName() + " is available, new version is " + getVersion() + ". Your installed version is " + this.plugin.getDescription().getVersion() + ".\nPlease update to the latest version :)");
    }
    this.plugin.getLogger().info(this.updateMessage);
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    if ((this.updateAvailable) && (event.getPlayer().isOp())) {
      event.getPlayer().sendMessage(this.updateMessage);
    }
    if ((this.pluginMessage != null) && (event.getPlayer().isOp())) {
      event.getPlayer().sendMessage(this.pluginMessage);
    }
  }
}
