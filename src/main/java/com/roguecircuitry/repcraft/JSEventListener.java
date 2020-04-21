package com.roguecircuitry.repcraft;

import org.graalvm.polyglot.Value;

public class JSEventListener {
  public String type;
  public Value func;
  public JSEventListener (String eType, Value cb) {
    this.type = eType;
    this.func = cb;
  }
}