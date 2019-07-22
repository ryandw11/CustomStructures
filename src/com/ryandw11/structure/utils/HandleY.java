package com.ryandw11.structure.utils;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class HandleY {
	
	/**
	 * Calculate the y value for a structure.
	 * @param cs The configurationsection of the schematic
	 * @param height The current height at that area.
	 * @return the height. (Will never be null)
	 */
	public static int calculateY(ConfigurationSection cs, int height) {
		String value = cs.getString("SpawnY");
		
		if(value.equalsIgnoreCase("top")) {
			return height;
		}
		
		if(value.contains("[")) {
			//If +[num-num]
			if(value.startsWith("+")) {
				String v = value.replace("[", "").replace("]", "").replace("+", "");
				String[] out = v.split("-");
				try {
					int num1 = Integer.parseInt(out[0]);
					int num2 = Integer.parseInt(out[1]);
					
					Random r = new Random();
					
					int a = r.nextInt(num2) + (num1 + 1);
					return height + a;
					
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
					return height;
				}
			}
			// if -[num-num]
			else if(value.startsWith("-")) {
				String v = value.replace("[", "").replace("]", "").replace("-", "");
				String[] out = v.split("-");
				try {
					int num1 = Integer.parseInt(out[0]);
					int num2 = Integer.parseInt(out[1]);
					
					Random r = new Random();
					
					int a = r.nextInt(num2) + (num1 + 1);
					return height - a;
					
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
					return height;
				}
			}
			// if just [num-num]
			else {
				String v = value.replace("[", "").replace("]", "");
				String[] out = v.split("-");
				try {
					int num1 = Integer.parseInt(out[0]);
					int num2 = Integer.parseInt(out[1]);
					
					Random r = new Random();
					
					int a = r.nextInt(num2 + 1) + num1;
					return a;
					
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
					return height;
				}
			}
		}
		// if +num
		else if(value.startsWith("+")) {
			String v = value.replace("+", "");
			try {
				int num = Integer.parseInt(v);
				return height + num;
			}catch(NumberFormatException ex) {
				return height;
			}
		}
		// if -num
		else if(value.startsWith("-")) {
			String v = value.replace("-", "");
			
			try {
				int num = Integer.parseInt(v);
				return height - num;
			}catch(NumberFormatException ex) {
				return height;
			}
		}
		// if just num
		else {
			try {
				return Integer.parseInt(value);
			} catch(NumberFormatException ex) {
				return height;
			}
		}
	}

}
