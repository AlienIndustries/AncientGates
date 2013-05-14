package org.mcteam.ancientgates.listeners;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.util.Vector;
import org.mcteam.ancientgates.Conf;
import org.mcteam.ancientgates.Gate;
import org.mcteam.ancientgates.Plugin;
import org.mcteam.ancientgates.util.EntityUtil;
import org.mcteam.ancientgates.util.ItemStackUtil;
import org.mcteam.ancientgates.util.TeleportUtil;

public class PluginMessengerListener implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String channel, Player unused, byte[] message) {
		if (!Conf.bungeeCordSupport || !channel.equals("BungeeCord")) {
			return;
		}
		
		// Get data from message
		String inChannel;
		byte[] data;
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
			inChannel = in.readUTF();
			short len = in.readShort();
			data = new byte[len];
			in.readFully(data);
		} catch (IOException e) {
			Plugin.log.severe("Error receiving BungeeCord message");
			e.printStackTrace();
			return;
		}
		
		// Parse BungeeCord teleport packet
		if (inChannel.equals("AGBungeeTele")) {
			// Data should be player name, and destination location
			String msg = new String(data);
			String[] parts = msg.split("#@#");
			
			String playerName = parts[0];
			String destination = parts[1];
			
			// Check if the player is online, if so, teleport, otherwise, queue
			Player player = Bukkit.getPlayer(playerName);
			if (player == null) {
				Plugin.bungeeCordPlayerInQueue.put(playerName.toLowerCase(), destination);
				// Block join message if queued
				Plugin.bungeeCordBlockJoinQueue.add(playerName.toLowerCase());
			} else {
				// Teleport incoming BungeeCord player
				Location location = TeleportUtil.stringToLocation(destination);
				TeleportUtil.teleportPlayer(player, location);
			}
		// Parse BungeeCord vehicle teleport packet
		} else if (inChannel.equals("AGBungeeVehicleTele")) {
			// Data should be player name, vehicle typeId, velocity and destination location
			String msg = new String(data);
			String[] parts = msg.split("#@#");
			
			String playerName = parts[0];
			int vehicleTypeId = Integer.parseInt(parts[1]);
			double velocity = Double.parseDouble(parts[2]);
			String destination = parts[3];
			
			// Check if the player is online, if so, teleport, otherwise, queue
			Player player = Bukkit.getPlayer(playerName);
			if (player == null) {
				Plugin.bungeeCordPassengerInQueue.put(playerName.toLowerCase(), String.valueOf(vehicleTypeId)+"#@#"+String.valueOf(velocity)+"#@#"+destination);
				// Block join message if queued
				Plugin.bungeeCordBlockJoinQueue.add(playerName.toLowerCase());
			} else {
				// Teleport incoming BungeeCord player
				Location location = TeleportUtil.stringToLocation(destination);
				TeleportUtil.teleportVehicle(player, vehicleTypeId, velocity, location);
			}
		// Parse BungeeCord spawn packet
		} else if (inChannel.equals("AGBungeeSpawn")) {
			// Data should be entitytype id, entitytype data and destination location
			String msg = new String(data);
			String[] parts = msg.split("#@#");
				
			int entityTypeId = Integer.parseInt(parts[0]);
			String entityTypeData = parts[1];
			String destination = parts[2];
				
			// Spawn incoming BungeeCord entity
			Location location = TeleportUtil.stringToLocation(destination);
			World world = TeleportUtil.stringToWorld(destination);

			if (EntityType.fromId(entityTypeId).isSpawnable()) {
				Entity entity = world.spawnEntity(location, EntityType.fromId(entityTypeId));
				EntityUtil.setEntityTypeData(entity, entityTypeData);
				entity.teleport(location);
			}	
		// Parse BungeeCord vehicle spawn packet
		} else if (inChannel.equals("AGBungeeVehicleSpawn")) {
			// Data should be vehicletype id, velocity, destination location, entitytype id and entitytype data
			String msg = new String(data);
			String[] parts = msg.split("#@#");
				
			int vehicleTypeId = Integer.parseInt(parts[0]);
			double velocity = Double.parseDouble(parts[1]);
			String destination = parts[2];
			
			Location location = TeleportUtil.stringToLocation(destination);
			World world = TeleportUtil.stringToWorld(destination);
			
			Entity passenger = null;
			String entityItemStack = null;

			// Parse passenger info
			if (parts.length > 4) {
				int entityTypeId = Integer.parseInt(parts[3]);
				String entityTypeData = parts[4];

				if (EntityType.fromId(entityTypeId).isSpawnable()) {
					// Spawn incoming BungeeCord entity
					passenger = world.spawnEntity(location, EntityType.fromId(entityTypeId));
					EntityUtil.setEntityTypeData(passenger, entityTypeData);
					passenger.teleport(location);
				}
			// Parse vehicle contents
			} else if (parts.length > 3) {
				entityItemStack = parts[3];
			}
			final Entity p = passenger;
				
			// Create new velocity
			final Vector newVelocity = location.getDirection();
			newVelocity.multiply(velocity);

			// Spawn incoming BungeeCord vehicle
			if (passenger != null) {
				final Vehicle v = (Vehicle)location.getWorld().spawnEntity(location, EntityType.fromId(vehicleTypeId));
				Plugin.instance.getServer().getScheduler().scheduleSyncDelayedTask(Plugin.instance, new Runnable() {
					public void run() {
						if (p != null) v.setPassenger(p);
						v.setVelocity(newVelocity);
					}
				}, 2);
			} else {
				Vehicle mc = (Vehicle)location.getWorld().spawnEntity(location, EntityType.fromId(vehicleTypeId));
				if (mc instanceof StorageMinecart && entityItemStack != null) {
					StorageMinecart smc = (StorageMinecart)mc;
					smc.getInventory().setContents(ItemStackUtil.stringToItemStack(entityItemStack));
				} else if (mc instanceof HopperMinecart && entityItemStack != null) {
					HopperMinecart hmc = (HopperMinecart)mc;
					hmc.getInventory().setContents(ItemStackUtil.stringToItemStack(entityItemStack));
				}
				mc.setVelocity(newVelocity);
			}
		// Parse BungeeCord command packet
		} else if (inChannel.equals("AGBungeeCom")) {
			// Data should be server, command, id and command data
			String msg = new String(data);
			String[] parts = msg.split("#@#");
			
			String command = parts[0];
			String player = parts[1];
			String server = parts[2];
			String gateid = parts[3];
			String comdata = parts[4];
			
			// Parse "setto" command
			if (command.toLowerCase().equals("setto")) {
				if (Plugin.hasPermManage((Player) Plugin.instance.getServer().getOfflinePlayer(player), "ancientgates.setto.bungee")) {
					Gate gate = Gate.get(gateid);
					gate.setTo(null);
					gate.setBungeeTo(server, comdata);
					Gate.save();
				}
			}
		} else {
			return;
		}	
	}
	
}