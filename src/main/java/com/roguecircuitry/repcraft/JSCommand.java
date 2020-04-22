
package com.roguecircuitry.repcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    
    
    //Split the last space separated key up by object member access '.'
    // String[] toAutoComplete = args[args.length-1].split(".");
    // List<String> results = new ArrayList<String>();
    
    // Value lastFoundObj = null;
    // Value lastObj = null; //Start with global
    // Set<String> possibleKeys;
    // String currentSearchKey;

    // //Do a logic loop to complete to last possible key, and then also make guesses
    // for (int i=0; i<toAutoComplete.length; i++) {
    //   currentSearchKey = toAutoComplete[i];

    //   //Try and get a member of last object
    //   lastObj = this.master.get(lastObj, currentSearchKey);

    //   if (lastObj != null) {
    //     lastFoundObj = lastObj;
    //     continue;
    //   }
    //   if (lastFoundObj != null) {
        
    //   }

    //   if (lastObj != null) {
    //     lastFoundObj = lastObj;
    //   } else {
    //     //
    //     if (lastFoundObj == null) {
    //       //If we didn't match anything, try to get possible keys 
    //       possibleKeys = this.master.jsBinding.getMemberKeys();
    //       for (String key : possibleKeys) {
    //         if (key == "." || key == "" || key.contains(currentSearchKey)) {
    //           results.add(key);
    //         }
    //       }
    //       return results;
    //     } else {
    //       possibleKeys = lastFoundObj.getMemberKeys();
    //       for (String key : possibleKeys) {
    //         if (key.contains(currentSearchKey)) {
    //           results.add(key);
    //         }
    //       }
    //       return results;
    //     }
    //   }
    // }
    // return results;
  }
}