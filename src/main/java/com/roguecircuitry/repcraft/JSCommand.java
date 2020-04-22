
package com.roguecircuitry.repcraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.graalvm.polyglot.Value;

public class JSCommand implements CommandExecutor, TabCompleter {
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

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    //Split the last space separated key up by object member access '.'
    String toAutoComplete = args[args.length-1];
    List<String> results = new ArrayList<String>();
    
    Value f = (Value)this.master.eval("onTabComplete");
    Value result = f.execute(toAutoComplete);
    for (int i=0; i<result.getArraySize(); i++) {
      results.add( result.getArrayElement(i).asString() );
    }
    return results;
  }
}