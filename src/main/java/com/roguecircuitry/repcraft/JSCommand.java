package com.roguecircuitry.repcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JSCommand implements CommandExecutor {
  RepCraft master;

  public JSCommand(RepCraft master) {
    super();
    this.master = master;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    String toExecute = String.join(" ", args);
    Player p = null;
    
    if (sender instanceof Player) {
      p = (Player) sender;
      this.master.put("self", p);
    }

    try {
      String result = "[js] " + this.master.eval(toExecute).toString();
      if (p == null) {
        System.out.println(result);
      } else {
        p.sendMessage(result);
      }
      return true;
    } catch (Exception ex) {
      System.out.println(ex);
      return false;
    }
  }
}