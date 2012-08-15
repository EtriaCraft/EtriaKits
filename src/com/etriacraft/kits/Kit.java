package com.etriacraft.kits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.bukkit.inventory.ItemStack;

public class Kit {
	
	private String name;
	private int cooldown = 0;
	private boolean error = false;
	private ArrayList<HashMap<String, Object>> contents = new ArrayList();
	private ArrayList<ItemStack> iss = new ArrayList();
	
	public Kit(String name, String raw) {
		this.name = name;
		Pattern p = Pattern.compile(";", Pattern.LITERAL);
		
		try {
			for (String seg : p.split(raw)) {
				//Item Time!
				if (seg.contains(" ")) {
					String[] info = seg.split(" ");
					int amount = Integer.parseInt(info[1]);
					int id;
					short damage = 0;
					
					if (info[0].contains(":")) {
						String[] data = info[0].split(":");
						id = Integer.parseInt(data[0]);
						damage = Short.parseShort(data[1]);
					} else {
						id = Integer.parseInt(info[0]);
					}
					
					this.iss.add(new ItemStack(id, amount, damage));
					HashMap<String, Object> hm = new HashMap();
					hm.put("id", id);
					hm.put("amount", amount);
					hm.put("damage", damage);
					this.contents.add(hm);
					continue;
				}
				
				//Cooldowns
				if (seg.startsWith("-")) {
					this.cooldown = Integer.parseInt(seg.substring(1));
					continue;
				}
				
				throw new Exception();
			}
		} catch (Exception e) {
			this.error = true;
			Main.log.severe("Error in the configuration of kit: " + name + " please fix kits.properties");
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getCooldown() {
		return this.cooldown;
	}
	
	public ArrayList<HashMap<String, Object>> getContents() {
		return this.contents;
	}
	
	public ArrayList<ItemStack> getItemStacks() {
		return this.iss;
	}
	
	public boolean hasErr() {
		return this.error;
	}
	
	public String getPermission() {
		return "kit." + this.name;
	}
	
}