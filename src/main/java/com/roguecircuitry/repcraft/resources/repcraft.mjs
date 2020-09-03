
const Bukkit = Java.type('org.bukkit.Bukkit');
const Command = Java.extend(Java.type('org.bukkit.command.Command'));

const File = Java.type("java.io.File");
const System = Java.type("java.lang.System");
const Source = org.graalvm.polyglot.Source;

const server = Bukkit.getServer();
const manager = server.getPluginManager();
const plugin = manager.getPlugin("RepCraft");

//grabbed from grakkit/core
const commandMap = server.getClass().getDeclaredField("commandMap");
commandMap.setAccessible(true);
const registry = commandMap.get(server);

function getCommand (name) {
  return registry.getCommand(name);
}

function createCommand (name, exec, tab) {
  if (!name) throw `Must provide valid name, was ${name}`;
  if (!exec) throw `Must provide execute method`;
  if (typeof(exec) != "function") throw `execute must be a type of function, was ${typeof(exec)}`;
  // let cmd = new Command​(name, {execute:execute,onTabComplete:tab });
  // registry.register("RepCraft", cmd);
  // return cmd;
  let cmd = new Command(name, {
    execute: exec
  });
  registry.register("repcraft", cmd);
  return cmd;
}

createCommand("js", (sender, cmd, args)=>{
  // sender.sendMessage("YAY IT WORKS");
  let code = "";
  for (let i=0; i<args.length; i++) {
    code += args[i] + " ";
  }
  let res = globalThis["repcraft"].eval(code);
  console.log(res);
  return true;
});
globalThis.createCommand = createCommand;

/**@param {any} byteBuffer
 * @returns {ArrayBuffer}
 */
function byteBufferToArrayBuffer(byteBuffer) {
  if (!byteBuffer) throw `bytes were ${byteBuffer}, length ${byteBuffer.length}, cannot read`;
  let result = new Int8Array(byteBuffer.length);
  for (let i = 0; i < byteBuffer.length; i++) {
    result[i] = byteBuffer[i];
  }
  return result.buffer;
}

function fread(url) {
  const f = new java.io.File(url);
  if (!f.exists()) throw `Can't read url ${url}, file doesn't exist`;

  return java.nio.file.Files.readAllBytes(f.toPath());
}

function byteBufferToString(bytes) {
  return new java.lang.String(bytes);
}

function graaljsBullShitDynamicImportPolyfill(fpath) {
  let source = new File(fpath);

  let src;
  let mod;
  try {
    src = Source.newBuilder("js", source).mimeType("application/javascript+module").build();
    mod = globalThis["java-js-ctx"].eval(src);
  } catch (e) {
    console.log(e);
    return false;
  }
  return mod;
}

globalThis["import"] = graaljsBullShitDynamicImportPolyfill;

const fileRoot = new File(System.getProperty("user.dir"));
const fileRepCraft = new File(fileRoot, "repcraft");
if (!fileRepCraft.exists()) {
  if (!fileRepCraft.mkdir()) throw "Couldn't create server/repcraft dir";
}
const filePlugins = new File(fileRoot, "repcraft/js-plugins");
if (!filePlugins.exists()) {
  if (!filePlugins.mkdir()) throw "Couldn't create server/repcraft/js-plugins dir";
}

const importModules = () => {
  console.log("repcraft.mjs will now import modules from", filePlugins.getAbsolutePath());
  let subFiles = filePlugins.listFiles();
  let filePkg;
  let f;
  for (let i = 0; i < subFiles.length; i++) {
    f = subFiles[i];
    // console.log(f.getAbsolutePath());
    if (!f.isDirectory()) continue;
    filePkg = new File(f, "package.json");
    if (!filePkg.exists()) {
      console.log(`Skipping ${f.getName()}, no package.json`);
      continue;
    }
    let jsonPkg = JSON.parse(
      byteBufferToString(
        fread(`${f.getAbsolutePath()}/package.json`)
      )
    );
    console.log(f.getName(), JSON.stringify(jsonPkg));
    if (jsonPkg.main && typeof (jsonPkg.main) == "string") {
      let mpath = `${f.getAbsolutePath()}/${jsonPkg.main}`;
      // console.log("dyn import", mpath);
      let mod = globalThis.import(mpath);
      // console.log(`dyn import returns ${mod}`);
    } else {
      console.log("skipping import of", f.getAbsolutePath(), "no valid main field in package.json");
    }
  }
}

importModules();

