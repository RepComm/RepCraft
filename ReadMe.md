# RepCraft
Modern JavaScript for modern minecraft

## Disclaimers
This project is in pre-release state.<br/>
Feature may work, but are not fully tested<br/>
Be aware that ScriptCraft compatibility is not a priority<br/>

## Why is this a thing?
This project is addresses issues I have with scriptcraft:

- No support for modern JS (const, let, class, arrow-functions, es modules ..)
- Unfinished auto-complete
- Not enough debugging capabilities
- Not enough documentation
- Relies on deprecated java APIs
- Evals scripts that don't need to be loaded

This project is a SpigotMC API `JavaPlugin`<br/>
It gets its capabilities from GraalVM, a modern polyglot virtual machine<br/>
with support for a BUNCH of languages, including Java, and JavaScript.

## Current features
- ES Module `package.json` based plugins
- Java interop
- /js live eval command

## Planned features
- Sandboxed permissions per-plugin
- Persistent storage per-plugin
- Debug hot-reloading (file-watch) per-plugin
- Persistent debug state (after reload, states can be recovered)
- WebSocket / WebSocketServer
- Non-blocking TCP/UDP sockets
- WebWorker implementation

## Installing
This plugin makes use of GraalVM<br/>
You need to run spigot using Graal's JVM

TODO - Write graal instructions for linux/windows

## Basic Usage
`/js <code>` is the command registered

Example: `/js self` will echo the Player instance:<br/>
`[js] CraftPlayer{name=PlayerName}`

```javascript
/js self.sendMessage("Hello World");
```

## Basic plugin
Plugins are made up very similar to npm packages.<br/>

NOTE: Plugin directories go in `<spigot>/repcraft/js-plugins`

You create a directory for your code:<br/>
`mkdir my-plugin`<br/>

Go into it:<br/>
`cd my-plugin`<br/>

Create a JSON file called package:<br/>
`package.json` - should look like:<br/>

```json
{
  "main":"./index.js",
  "enabled":true
}
```
`main` points to our main script to be executed/treated as a plugin.<br/>
`enabled` is optional, if you need to disable the plugin, set to false

For a list of plugin package.json options, see [package.json docs](todo-url-here)

Lastly, lets create the code in the same folder:<br/>
`index.js` - Example code:
```javascript
console.log("Hello from my-plugin!");
```