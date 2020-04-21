package com.roguecircuitry.repcraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.plugin.RegisteredListener;
import org.graalvm.polyglot.Value;

public class JSEvents implements Listener {
  RepCraft master;
  public List<JSEventListener> listeners;

  public JSEvents(RepCraft master) {
    this.master = master;

    this.listeners = new ArrayList<JSEventListener>();

    this.master.jsBinding.putMember("Events", this);
    RegisteredListener registeredListener = new RegisteredListener(
      this,
      (listener, event) -> onEvent(event),
      EventPriority.NORMAL,
      master,
      false
    );

    for (HandlerList handler : HandlerList.getHandlerLists()) {
      handler.register(registeredListener);
    }
  }

  public void onEvent (Event e) {
    if (e instanceof EntityAirChangeEvent) return;
    for (JSEventListener l : this.listeners) {
      if (l.type.equals(e.getClass().getSimpleName())) {
        l.func.execute(e);
      }
    }
  }

  public void on (Class classType, Value f) {
    this.listeners.add(new JSEventListener(classType.getSimpleName(), f));
  }
}