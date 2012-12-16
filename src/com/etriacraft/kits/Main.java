package com.etriacraft.kits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
    public static final Logger log = Logger.getLogger("Minecraft");
    public TreeMap<String, Kit> kits = new TreeMap();
    public static final String prefix = "[EtriaKits] ";
    Methods methods;

    @Override
    public void onEnable() {
        loadKits();
        methods = new Methods(this);
    }
    
    //Create & load kit.properties. Create other necessary files.
    public void loadKits() {
        kits.clear();
        Properties props = new Properties();
        File f = new File(this.getDataFolder(), "players");
        if (!f.exists()) f.mkdirs();
        
        f = new File(this.getDataFolder(), "kits.properties");
        if (!f.exists()) {
            try {
                f.createNewFile();
                
                //Set defaults
                props.setProperty("starter", "17 64;17:2 64;-600");
                props.setProperty("advanced", "278 1;264 5;-86400");
                props.setProperty("rock", "1 256");
                props.store(new FileOutputStream(f), "## Format: NAME=ID AMNT;ID AMNT[;-cooldown] ###");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            props.load(new FileInputStream(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for (Entry<?, ?> e : props.entrySet()) {
            String key = (String) e.getKey(), value = (String) e.getValue();
            this.kits.put(key.toLowerCase(), new Kit(key.toLowerCase(), value));
        }
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
         if (args.length < 1) {
            //List kits
            String list = "";
            for (Kit kit : kits.values()) {
                if (!s.hasPermission(kit.getPermission())) continue;
                if (!list.isEmpty()) list += "§e,§a ";
                list += kit.getName();
            }

            if (list.isEmpty()) {
                s.sendMessage("§cYou don't have access to any kits");
                return true;
            }

            s.sendMessage("§aKits: " + list);
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!s.hasPermission("kit.reload")) return false;
                loadKits();
                log.info(prefix + "Kits reloaded.");
                s.sendMessage("§aKits reloaded");
                return true;
            }

            if (!kits.containsKey(args[0].toLowerCase())) {
                s.sendMessage("§cThat kit does not exist");
                return true;
            }

            Kit kit = kits.get(args[0].toLowerCase());

            if (kit.hasErr()) {
                s.sendMessage("§cError in configuration of kit, contact an administrator");
                return true;
            }

            if (!s.hasPermission(kit.getPermission())) {
                s.sendMessage("§cYou don't have permission to use this kit");
                return true;
            }

            //See who gets the kit
            Player receiver;
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("info")) {
                    s.sendMessage("§aKit§e " + kit.getName() + " §acontents:");
                    s.sendMessage(Methods.getItemList(kit.getItemStacks()));
                    return true;
                }

                receiver = Bukkit.getPlayer(args[1]);

                if (receiver == null) {
                    s.sendMessage("§7" + args[1] + " §cis not online");
                    return true;
                }
            } else {
                receiver = (Player) s; 
            }

            //Check cooldowns
            int now = (int) (System.currentTimeMillis() / 1000);
            int timeLeft = methods.getCooldown(s.getName(), kit.getName()) - now;
            if (timeLeft > 0 && !s.hasPermission("kit.bypass")) {
                s.sendMessage("§cPlease try in about " + Methods.timeUntil(timeLeft));
                return true;
            }

            //Dispense items
            for(HashMap hm : kit.getContents()) {
                HashMap<Integer, ItemStack> leftover = 
                        receiver.getInventory().addItem(
                        new ItemStack((int) hm.get("id"), (int) hm.get("amount"), (short) hm.get("damage")));
                if (!leftover.isEmpty()) {
                    //Drop items they couldn't hold onto the ground
                    for (ItemStack left : leftover.values()) {
                        receiver.getWorld().dropItemNaturally(receiver.getLocation(), left);
                    }
                }
            }

            //Send completion message
            if (receiver != s) {
                s.sendMessage("§aKit§e " + kit.getName() + " §agiven to§e " + receiver.getName());
                receiver.sendMessage("§aYou have received kit§e " + kit.getName() + " §afrom§e " + s.getName());
            } else {
                s.sendMessage("§aReceived kit§e " + kit.getName());
            }

            //Update player history, complete!
            methods.setCooldown(s.getName(), kit.getName(), (now + kit.getCooldown()));
        }
        return true;
    }
    
}