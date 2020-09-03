package com.roguecircuitry.repcraft;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.roguecircuitry.repcraft.resources.DefaultResources;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.Source;

/**
 * RepCraft main Spigot plugin class Most of the core logic is in here
 */
public final class RepCraft extends JavaPlugin {
  Context ctx;
  Value jsBinding;

  private void critial(String msg) {
    System.err.println("[RepCraft][CRITICAL] " + msg);
    getServer().getPluginManager().disablePlugin(this);
  }

  private void log(String msg) {
    System.out.println("[RepCraft][log] " + msg);
  }

  @Override
  public void onEnable() {
    File dirRepCraft = Paths.get("repcraft").toAbsolutePath().toFile();
    File dirPlugins = new File(dirRepCraft, "js-plugins");

    List<File> sources = new ArrayList<File>();

    if (!dirPlugins.exists()) {
      if (dirPlugins.mkdirs()) {
        this.log("created js-plugins dir at " + dirPlugins.getAbsolutePath());
        this.log("extracting built-in js");

        String internalResPath = "/com/roguecircuitry/repcraft/resources/";

        // Unpack necessary js
        File unpacked = new File(dirRepCraft, "repcraft.mjs");
        DefaultResources.unpackResTo(internalResPath + "repcraft.mjs", unpacked, true);
        this.log("extracted repcraft.mjs to " + unpacked.getAbsolutePath());
        sources.add(unpacked);

      } else {
        this.critial("could not create dir path for " + dirPlugins.getAbsolutePath());
        return;
      }
    } else {
      File unpacked = new File(dirRepCraft, "repcraft.mjs");
      sources.add(unpacked);
    }

    // Initialize the JavaScript engine/bindings
    this.log("init graalvm js context");
    this.ctx = Context.newBuilder("js").allowAllAccess(true).build();
    this.jsBinding = this.ctx.getBindings("js");

    this.put("java-js-ctx", this.ctx);
    this.put("repcraft", this);

    this.log("init bukkit event listeners");
    getServer().getPluginManager().registerEvents(new JSEvents(this), this);

    for (File s : sources) {
      this.log("running built-in source " + s.getAbsolutePath());
      if (this.loadSource(s, true) == null) {
        this.critial("failed to execute built-in source " + s.getAbsolutePath());
        return;
      }
    }
  }

  public Value dynImport (String fpath) {
    return this.loadSource(new File(fpath), true);
  }

  public Value loadSource(File source, Boolean isModule) {
    if (!source.exists()) return null;
    Source src;
    Value result;
    try {
      if (isModule) {
        src = Source.newBuilder("js", source).mimeType("application/javascript+module").build();
      } else {
        src = Source.newBuilder("js", source).build();
      }
      result = this.ctx.eval(src);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return result;
  }

  public Object eval(String js) {
    return this.ctx.eval("js", js);
  }

  public void put(String key, Object value) {
    this.jsBinding.putMember(key, value);
  }

  /**
   * Get a variable from a context If `from` is not supplied, it tries to fetch
   * from the jsBinding (global)
   * 
   * @param from context (typically null or an object containing the key)
   * @param key  of from or global to access with
   * @return null if non-existent, value accessable by the key
   */
  public Value get(Value from, String key) {
    if (from == null) {
      if (this.jsBinding.hasMember(key)) {
        return this.jsBinding.getMember(key);
      } else {
        return null;
      }
    }
    if (from.hasMember(key)) {
      return from.getMember(key);
    } else {
      return null;
    }
  }

  @Override
  public void onDisable() {
  }
}
