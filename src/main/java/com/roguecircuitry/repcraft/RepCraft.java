package com.roguecircuitry.repcraft;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
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
  JSCommand jsc;
  Value jsBinding;
  File pluginFile, pluginsDir, jsRepCraftDir, jsPluginsDir;

  JsonParser jsonParser;

  private void critial(String msg) {
    System.err.println("[RepCraft][CRITICAL] " + msg);
    getServer().getPluginManager().disablePlugin(this);
  }

  @Override
  public void onEnable() {
    // Initialize the JavaScript engine/bindings
    System.out.println("[RepCraft] Initializing GraalVM js context!");
    this.ctx = Context.newBuilder("js").allowAllAccess(true).build();
    this.jsBinding = this.ctx.getBindings("js");

    getServer().getPluginManager().registerEvents(new JSEvents(this), this);

    // Create a JSON parser so we can load package.json's for all of the plugins
    this.jsonParser = new JsonParser();

    File repcraftDir = Paths.get("repcraft").toAbsolutePath().toFile();
    List<File> sources = new ArrayList<File>();
    if (!repcraftDir.exists()) {
      if (!repcraftDir.mkdir()) {
        this.critial("Could not create <spigot>/repcomm ! This is required. Disabling.");
        return;
      }
      String resFromPath = "/com/roguecircuitry/repcraft/resources/";

      // Unpack necessary js
      File unpacked = new File(repcraftDir, "repcraft.mjs");
      DefaultResources.unpackResTo(resFromPath + "repcraft.mjs", unpacked, true);
      sources.add(unpacked);
    } else {
      sources.add(new File(repcraftDir, "repcraft.mjs"));
    }
    for (File s : sources) {
      if (this.loadSource(s)) {
        System.err.println("Loaded " + s.getAbsolutePath());
      }
    }

    File jsPluginsDir = Paths.get("repcraft/js-plugins").toAbsolutePath().toFile();

    if (jsPluginsDir.exists()) {
      this.loadPluginsFrom(jsPluginsDir);
    } else {
      if (jsPluginsDir.mkdirs()) {
        System.out.println("[RepCraft] Created " + jsPluginsDir.getAbsolutePath());
      } else {
        this.critial("Failed to create all of the necessary dirs for <spigot>/repcraft/js-plugins");
      }
    }
    jsc = new JSCommand(this);

    this.getCommand("js").setExecutor(this.jsc);
    this.getCommand("js").setTabCompleter(this.jsc);
  }

  public void loadPluginsFrom(File jsPluginsDir) {
    File[] subdirs = jsPluginsDir.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory();
      }
    });

    this.loadPlugins(subdirs);
  }

  public void loadPlugins(File[] subdirs) {
    File pluginSubDir, packageJson;
    for (int i = 0; i < subdirs.length; i++) {
      pluginSubDir = subdirs[i];
      packageJson = new File(pluginSubDir.getAbsoluteFile() + "/package.json");

      if (this.loadPlugin(packageJson, pluginSubDir)) {
        System.out.println("[RepCraft] Loaded plugin " + packageJson.getName());
      }
    }
  }

  public boolean loadPlugin(File packageJson, File pluginSubDir) {
    JsonObject pkgJson;
    String pkgJsonMain;
    File jsPluginFile;

    try {
      pkgJson = this.jsonParser.parse(new JsonReader(new FileReader(packageJson))).getAsJsonObject();
    } catch (Exception ex) {
      // Suppress, file doesn't exist , no biggy :)
      return false;
    }
    // Skip package.json that don't have "main" in them
    if (!pkgJson.has("main"))
      return false;

    pkgJsonMain = pkgJson.get("main").getAsString();

    jsPluginFile = new File(pluginSubDir.getAbsoluteFile() + "/" + pkgJsonMain);

    if (!this.loadSource(jsPluginFile)) {
      System.err.println("Couldn't import 'main' : " + jsPluginFile.toPath() + ", ignoring!");
      return false;
    }
    return true;
  }

  public boolean loadSource(File source) {
    if (!source.exists())
      return false;
    try {
      Source src = Source.newBuilder("js", source).build();
      this.ctx.eval(src);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public Object eval(String js) {
    return this.ctx.eval("js", js);
  }

  public void put(String key, Object value) {
    this.jsBinding.putMember(key, value);
  }

  /**Get a variable from a context
   * If `from` is not supplied, it tries to fetch from the jsBinding (global)
   * @param from context (typically null or an object containing the key)
   * @param key of from or global to access with
   * @return null if non-existent, value accessable by the key
   */
  public Value get (Value from, String key) {
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
