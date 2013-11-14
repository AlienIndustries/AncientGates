package org.mcteam.ancientgates.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.mcteam.ancientgates.Conf;

public class TextUtil {
	
	public static Map<String, ChatColor> chatColors;
	static {
		chatColors = new HashMap<String, ChatColor>();
		  for (ChatColor c : ChatColor.values()) {
			  chatColors.put(c.name(), c);
		  }
	}
	
	public static String titleize(String str) {
		String line = Conf.colorChrome+repeat("_", 60);
		String center = ".[ " + Conf.colorSystem + str + Conf.colorChrome + " ].";
		int pivot = line.length() / 2;
		int eatLeft = center.length() / 2;
		int eatRight = center.length() - eatLeft;
		return line.substring(0, pivot - eatLeft) + center + line.substring(pivot + eatRight);
	}
	
	public static String repeat(String s, int times) {
	    if (times <= 0) return "";
	    else return s + repeat(s, times-1);
	}
	
	public static ArrayList<String> split(String str) {
		return new ArrayList<String>(Arrays.asList(str.trim().split("\\s+")));
	}
	
	public static Boolean containsSubString(List<String> strList, String subStr) {
		for (String str : strList) {
			if (str.contains(subStr)) return true;
		}
		return false;
	}
	
	public static String implode(List<String> list1, List<String> list2, List<String> list3, String glue) {
	    String ret = "";
	    for (int i=0; i<list1.size(); i++) {
	        if (i!=0) {
	        	ret += glue;
	        }
	        ret += list1.get(i)+" ("+list2.get(i)+" - "+list3.get(i)+")";
	    }
	    return ret;
	}
	
	public static String implode(List<String> list1, List<String> list2, String glue) {
	    String ret = "";
	    for (int i=0; i<list1.size(); i++) {
	        if (i!=0) {
	        	ret += glue;
	        }
	        ret += list1.get(i)+" ("+list2.get(i)+")";
	    }
	    return ret;
	}
	
	public static String implode(List<String> list, String glue) {
	    String ret = "";
	    for (int i=0; i<list.size(); i++) {
	        if (i!=0) {
	        	ret += glue;
	        }
	        ret += list.get(i);
	    }
	    return ret;
	}
	
	public static String implode(List<String> list) {
		return implode(list, " ");
	}
	
	public static String implode(Map<String, Material> list, String glue) {
	    String ret = "";
	    int i = 0;
	    Set<String> keys = list.keySet();
	    for (String key : keys) {
	        if (i++!=0) {
	        	ret += glue;
	        }
	        ret += key;
	    }
	    return ret;
	}
	
	public static String md5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; i++) {
				sb.append(Integer.toHexString(array[i] & 0xFF | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// Ignore, will return null
		}
		return null;
	}
	
	public static boolean isBoolean(String s) {
	    return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
}


