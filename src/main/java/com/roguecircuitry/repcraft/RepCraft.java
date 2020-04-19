package com.roguecircuitry.repcraft;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * Hello world!
 */
public final class RepCraft extends JavaPlugin {
  Context ctx;
  JSCommand jsc;
  Value jsBinding;

  @Override
  public void onEnable () {
    System.out.println("[RepCraft] Initializing GraalVM js context!");
    this.ctx = Context.newBuilder("js").allowAllAccess(true).build();
    this.jsBinding = this.ctx.getBindings("js");

    try {
      this.ctx.eval("js", "print('[js] Hello World');");
    } catch (Exception ex) {
      System.err.println(ex);
      return;
    }

    jsc = new JSCommand(this);

    this.getCommand("js").setExecutor(this.jsc);
  }

  public Object eval (String js) {
    return this.ctx.eval("js", js);
  }

  public void put (String key, Object value) {
    this.jsBinding.putMember(key, value);
  }

  @Override
  public void onDisable () {

  }
}
