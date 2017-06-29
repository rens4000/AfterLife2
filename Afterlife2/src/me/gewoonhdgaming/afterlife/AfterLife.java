package me.gewoonhdgaming.afterlife;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
	
@SuppressWarnings("deprecation")
public class AfterLife extends JavaPlugin implements Listener {
	
	public HashMap<String, Integer> al = new HashMap<>();
	
	public String PREFIX = ChatColor.AQUA + "After" + ChatColor.YELLOW + "Life " + ChatColor.WHITE;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		console.sendMessage(ChatColor.RED + "=-=-=-=-=-=-=-=-=");
		console.sendMessage(ChatColor.GREEN + "AfterLife");
		console.sendMessage(ChatColor.GREEN + "Version: 2.3");
		console.sendMessage(ChatColor.GREEN + "Created by: Boykev & Rens4000");
		console.sendMessage(ChatColor.GREEN + "Status: Enabled");
		console.sendMessage(ChatColor.RED + "=-=-=-=-=-=-=-=-=");
	}
	
	@Override
	public void onDisable() {
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		console.sendMessage(ChatColor.RED + "=-=-=-=-=-=-=-=-=");
		console.sendMessage(ChatColor.GREEN + "AfterLife");
		console.sendMessage(ChatColor.GREEN + "Version: 2.3");
		console.sendMessage(ChatColor.GREEN + "Created by: Boykev & Rens4000");
		console.sendMessage(ChatColor.GREEN + "Status: Disabled");
		console.sendMessage(ChatColor.RED + "=-=-=-=-=-=-=-=-=");
	}
	
	//EVENTS
	
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = (Player) e.getEntity();
		if(al.containsKey(p.getName())) {
			return;
		}
		//Do first time stuff
		al.put(p.getName(), 300);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
        p.sendTitle(ChatColor.AQUA + "U bent dood gegaan", ChatColor.RED + "U bent tijdelijk een geest");
        p.sendMessage(ChatColor.RED + "Je bent nu 5 minuten niet zichbaar voor andere spelers, ook kan je niet chatten, items opakken of commands uitvoeren. Na 5 minuten zal je weer levend zijn en kan je alles weer!");
		//Runnable
        new BukkitRunnable() {

			@Override
			public void run() {
				if(!p.isOnline()) { 
					this.cancel();
					return; 
				
				}
				int i = al.get(p.getName()) - 1;
				al.put(p.getName(), i);
				p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
				if(al.get(p.getName()) == 1) {
					al.remove(p.getName());
					p.removePotionEffect(PotionEffectType.INVISIBILITY);
                    p.sendMessage(ChatColor.AQUA + "Je bent weer levend! Veel succes.");
                    p.kickPlayer(ChatColor.RED + "Je bent weer levend! Rejoin de server zodat mensen je weer kunnen zien!");
                    
                    this.cancel();
                    return;
				}
				ActionBarAPI.sendActionBar(p,  ChatColor.WHITE + "Je bent nog voor " + ChatColor.AQUA + al.get(p.getName()) + ChatColor.WHITE + " secondes een geest!");
			}
			
		}.runTaskTimer(this, 0, 20);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(al.containsKey(p.getName())) {
			getConfig().set("deaths." + p.getName(), al.get(p.getName()));
			saveConfig();
			al.remove(p.getName());
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(getConfig().contains("deaths." + p.getName())) {
	        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
	        p.sendTitle(ChatColor.AQUA + "U bent dood gegaan", ChatColor.RED + "U bent tijdelijk een geest");
	        p.sendMessage(ChatColor.RED + "Je bent nu voor een paar minuten niet zichbaar voor andere spelers, ook kan je niet chatten, items opakken of commands uitvoeren. Na 5 minuten zal je weer levend zijn en kan je alles weer!");
	        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "god " + p.getName() + " on");
	        //Runnable
	        new BukkitRunnable() {
	        	
	        	int i = getConfig().getInt("deaths." + p.getName());

				@Override
				public void run() {
					al.put(p.getName(), i);
					i = i - 1;
					p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
					if(al.get(p.getName()) == 1) {
						al.remove(p.getName());
						p.removePotionEffect(PotionEffectType.INVISIBILITY);
						Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "god " + p.getName() + " off");
	                    p.sendMessage(ChatColor.AQUA + "Je bent weer levend! Veel succes.");
	                    p.kickPlayer(ChatColor.RED + "Je bent weer levend! Rejoin de server zodat mensen je weer kunnen zien!");
	                    getConfig().set("deaths." + p.getName(), null);
	                    this.cancel();
	                    return;
					}
					ActionBarAPI.sendActionBar(p,  ChatColor.WHITE + "Je bent nog voor " + ChatColor.AQUA + al.get(p.getName()) + ChatColor.WHITE + " secondes een geest!");
				}
				
			}.runTaskTimer(this, 0, 20);
		}
	}
	
	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent e) {
		if(al.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet praten! Je bent nog voor " + al.get(e.getPlayer().getName()) + " secondes in afterlive!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void godMode(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(al.containsKey(p.getName())) {
				p.sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet dood gaan! Je bent nog voor "
			+ al.get(p.getName()) + " secondes in afterlife!");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void itemPickup(PlayerPickupItemEvent e) {
		if(al.containsKey(e.getPlayer().getName())) {
			e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlive, dus kan je geen items oppakken! Je bent nog voor "
					+ al.get(e.getPlayer().getName()) + " secondes in afterlife!");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_AIR) {
			if(al.containsKey(p.getName())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet interacten! Je bent nog voor "
						+ al.get(e.getPlayer().getName()) + " secondes in afterlife!");
			}
         }
	}
	
	@EventHandler
	public void onChat(PlayerChatEvent ghg) {
		Player p = ghg.getPlayer();
			if(al.containsKey(p.getName())) {
				ghg.setCancelled(true);
				ghg.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet Chatten! Je bent nog voor "
						+ al.get(ghg.getPlayer().getName()) + " secondes in afterlife!");
			}
         }
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent  e) {
		Player p = e.getPlayer();
			if(al.containsKey(p.getName())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je Geen commando's uitvoeren! Je bent nog voor "
						+ al.get(e.getPlayer().getName()) + " secondes in afterlife!");
			}
         }

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
if (command.getName().equalsIgnoreCase("al") && sender instanceof Player){
			
			Player ghg = (Player) sender;
			if (args.length == 0) {
				if (ghg.hasPermission("AfterLife.user")) {
					ghg.sendMessage(ChatColor.RED + "Development by: Boykev en Rens");
					ghg.sendMessage(ChatColor.RED + "Product Owner: GewoonHDEnterprise");
					ghg.sendMessage(ChatColor.RED + "Copyright, contribution not allowed!");
			}
			}			
			else if (args[0].equalsIgnoreCase("info")) {
				if (ghg.hasPermission("AfterLife.user")) {
					ghg.sendMessage(ChatColor.RED + "AfterLife is een plugin waarin mensen tijdelijk dood zijn zonder deze uit de server te hoeven bannen");
			}
			}
			else if (args[0].equalsIgnoreCase("check")) {
				if (ghg.hasPermission("AfterLife.Admin")) {
					Player p = (Player) sender;
					p.sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet interacten! Je bent nog voor "
							+ al.get(p.getPlayer().getName()) + " secondes in afterlife!");
			}
			}
			else if (args[0].equalsIgnoreCase("reload")) {
				if (ghg.hasPermission("AfterLife.Admin")) {
					Player p = (Player) sender;
					p.sendMessage(ChatColor.RED + "Reloaden is nog niet mogelijk!");
				}
			}
			else {
				ghg.sendMessage(ChatColor.RED + "Er is wat fout gegaan!");
			}
			return true;
		}
		return false;	
		
	}


}
	


