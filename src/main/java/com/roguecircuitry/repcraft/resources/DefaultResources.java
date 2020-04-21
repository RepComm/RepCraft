package com.roguecircuitry.repcraft.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultResources {
  public static void unpackResTo(String resourceName, File resourceDestination, boolean log) {
    InputStream is = DefaultResources.getRes(resourceName);
    if (is == null) {
      System.err.println("Couldn't find resource to unpack: " + resourceName);
      return;
    }
    int readBytes = 0;
    byte[] buffer = new byte[4096];
    FileOutputStream fos = null;

    try {
      fos = new FileOutputStream(resourceDestination);
      while ((readBytes = is.read(buffer)) > 0) {
        fos.write(buffer, 0, readBytes);
      }
      if (log) {
        System.out.println("Unpacked " + resourceName);
      }
    } catch (FileNotFoundException fnfe) {
      System.err.println("Couldn't unpack " + resourceName + " to " + resourceDestination.getName() + " : " + fnfe);
    } catch (IOException e) {
      System.err.println("Error while unpacking " + resourceName + " to " + resourceDestination.getName() + " : " + e);
    } finally {
      try {
        fos.close();
        is.close();
      } catch (Exception e) {
        //Fuck off
      }
    }
    return;
  }

  public static InputStream getRes(String key) {
    return DefaultResources.class.getResourceAsStream(key);
  }
}