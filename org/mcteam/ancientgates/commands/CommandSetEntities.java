package org.mcteam.ancientgates.commands;

import org.mcteam.ancientgates.Gate;

public class CommandSetEntities extends BaseCommand {
	
	public CommandSetEntities() {
		aliases.add("setentities");
		
		requiredParameters.add("id");
		requiredParameters.add("true/false");
		
		requiredPermission = "ancientgates.setentities";
		
		senderMustBePlayer = false;
		
		helpDescription = "Set entity teleportion for gate.";
	}
	
	public void perform() {
		Boolean flag = Boolean.valueOf(parameters.get(1));
            
		gate.setTeleportEntities(flag);
		sendMessage("Entity teleportation for gate \""+gate.getId()+"\" is now "+String.valueOf(flag)+".");
		
		Gate.save();
	}
        
}