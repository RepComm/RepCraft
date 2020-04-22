
/**Get value from key (global or from passed `obj`)
 * @param obj that contains the key
 * @param key that references the value
 */
function getFromKey (obj, key) {
  if (obj) return obj[key];
  return globalThis[key];
}

/**
 * @param {String} toAutoComplete
 */
function onTabComplete (toAutoComplete) {
  let results = new Array();
  let objLinkKeys = toAutoComplete.split(".");

  let lastFoundObj = globalThis;
  let temp;
  let key;
  let foundKeys;

  for (let i=0; i<objLinkKeys.length; i++) {
    key = objLinkKeys[key];

    temp = getFromKey(lastFoundObj, key);
    if (temp) {
      lastFoundObj = temp;
      //TODO - also search for larger autocompletes if we're at the last key
      continue;
    }
    if (lastFoundObj) {
      foundKeys = Object.keys(lastFoundObj);
      for (let foundKey of foundKeys) {
        if (key === "." || key === "" || foundKey.includes(key)) {
          results.push(foundKey);
        }
      }
    }
  }
}

globalThis.onTabComplete = onTabComplete;
