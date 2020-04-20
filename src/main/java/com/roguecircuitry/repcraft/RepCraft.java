package com.roguecircuitry.repcraft;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

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
  File pluginFile, pluginsDir, jsRepCraftDir, jsPluginsDir;

  private void critial(String msg) {
    System.err.println("[RepCraft][CRITICAL] " + msg);
    getServer().getPluginManager().disablePlugin(this);
  }

  @Override
  public void onEnable() {
    System.out.println("[RepCraft] Initializing GraalVM js context!");
    this.ctx = Context.newBuilder("js").allowAllAccess(true).build();
    this.jsBinding = this.ctx.getBindings("js");

    File jsPluginsDir = Paths.get("repcraft/js-plugins").toAbsolutePath().toFile();
    
    if (!jsPluginsDir.exists()) {
      if (jsPluginsDir.mkdirs()) {
        System.out.println("[RepCraft] Created " + jsPluginsDir.getAbsolutePath());
      } else {
        this.critial("Failed to create all of the necessary dirs for <spigot>/repcraft/js-plugins");
        return;
      }
    }

    File[] subdirs = jsPluginsDir.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory();
      }
    });

    File pluginSubDir = null;
    File packageJson = null;
    JsonObject pkgJson = null;
    JsonParser parser = new JsonParser();
    String pkgJsonMain = null;
    File jsPluginFile = null;

    for (int i = 0; i < subdirs.length; i++) {
      pluginSubDir = subdirs[i];

      packageJson = new File(pluginSubDir.getAbsoluteFile() + "/package.json");
      try {
        pkgJson = parser.parse(
          new JsonReader(
            new FileReader(
              packageJson
            )
          )
        ).getAsJsonObject();

      } catch (Exception ex) {
        // Suppress, file doesn't exist , no biggy :)
        continue;
      }
      // Skip package.json that don't have "main" in them
      if (!pkgJson.has("main")) continue;

      pkgJsonMain = pkgJson.get("main").getAsString();
      
      jsPluginFile = new File(pluginSubDir.getAbsoluteFile() + "/" + pkgJsonMain);
      if (!jsPluginFile.exists()) {
        System.err.println("Couldn't import 'main' : " + jsPluginFile.toPath() + ", ignoring!");
        continue;
      }

      try {
        this.ctx.eval("js", new String(Files.readAllBytes(jsPluginFile.toPath())));
      } catch (Exception e) {
        //Technically we already handled this, just skip this script
        e.printStackTrace();
        continue;
      }
    }

    try {
      this.ctx.eval("js", "print('[js] Hello World');");
    } catch (Exception ex) {
      System.err.println(ex);
      return;
    }

    jsc = new JSCommand(this);

    this.getCommand("js").setExecutor(this.jsc);
  }

  public Object eval(String js) {
    return this.ctx.eval("js", js);
  }

  public void put(String key, Object value) {
    this.jsBinding.putMember(key, value);
  }

  @Override
  public void onDisable() {

  }
}
