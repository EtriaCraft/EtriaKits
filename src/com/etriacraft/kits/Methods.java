package com.etriacraft.kits;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.bukkit.inventory.ItemStack;

public class Methods {
    
    Main plugin;
    
    public Methods(Main instance) {
        this.plugin = instance;
    }
    
    public int getCooldown(String player, String kit) {
        Properties props = this.getHistory(player);
        try {
            return Integer.parseInt(props.getProperty(kit));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public void setCooldown(String player, String kit, int cooldown) {
        Properties props = this.getHistory(player);
        props.setProperty(kit, Integer.toString(cooldown));
        this.setHistory(props, player);
    }
    
    private Properties getHistory(String player) {
        Properties props = new Properties();
        File f = new File(plugin.getDataFolder(), "/players/" + player.toLowerCase() + ".history");
        
        try {
            props.load(new FileReader(f));
        } catch (FileNotFoundException e) {//Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return props;
    }
    
    private void setHistory(Properties props, String player) {
        File f = new File(plugin.getDataFolder(), "/players/" + player.toLowerCase() + ".history");
        if (!f.exists()) {
            try { f.createNewFile(); } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            props.store(new FileWriter(f), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getItemList(List<ItemStack> iss) {
        HashMap<String, Integer> items = new HashMap();
        String list = "§a";
        //Build cumulative hashmap
        for(ItemStack is : iss) {
            if (is == null) continue;
            int amm = 0;
            if (items.containsKey(is.getType().name())) {
                amm += (items.get(is.getType().name())) + is.getAmount();
                items.put(is.getType().name(), amm);
            } else {
                items.put(is.getType().name(), is.getAmount());
            }
        }
        //Build string
        for (Entry<String, Integer> map : items.entrySet()) {
            list += map.getValue() + " " + map.getKey().toLowerCase()+ "§e,§a ";
        }
        return list;
    }
    
    public static String timeUntil(int sec) {
        int buf;
        if (sec < 60*2) {
            buf = sec;
            return buf + " second" + (buf==1?"":"s"); 
        }
        if (sec < 3600*2) { 
            buf = sec/60;
            return buf + " minute" + (buf==1?"":"s"); 
        }
        if (sec < 86400*2) { 
            buf = sec/3600;
            return buf + " hour" + (buf==1?"":"s"); 
        }
        buf = sec/86400;
        return buf + " day" + (buf==1?"":"s");
    }
    
}