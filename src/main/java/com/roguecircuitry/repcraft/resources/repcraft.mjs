
//POLYFILL because no window object
globalThis.window = globalThis;
let window = globalThis;
globalThis.global = globalThis;
//END POLYFILL

const getProps = (obj) => {
  let properties = new Set();
  let currentObj = obj;
  do {
    Object.getOwnPropertyNames(currentObj).map(item => properties.add(item));
  } while ((currentObj = Object.getPrototypeOf(currentObj)))
  return [...properties.keys()];//.filter(item => typeof obj[item] === 'function')
}

window.getProps = getProps;

const keyToDesc = (obj, key) => {
  return key + " : " + typeof (obj[key]);
}

export class EvalComplete {
  constructor() {
    this.lastObj = undefined;
    this.potentialObj = undefined;
    this.prop = undefined;
    /**@type {Array<String>} */
    this.lastKeys = undefined;

    this.completeStrings = new Array();
  }

  /**Try to complete a string
   * Values that can complete are stored in completeStrings property
   * @param {string} str String to auto-complete
   * @param {boolean} allPropsOfStr true when trying to get all props of object string refers to
   * @returns {boolean} successful iteration or not (not necessarily fail when false)
   */
  complete(str, allPropsOfStr = false) {
    if (str[str.length - 1] == ".") allPropsOfStr = true;
    if (allPropsOfStr) {
      str = str.substring(0, str.length - 1);
      try {
        //Try to get an object represented by the string first
        this.potentialObj = eval(str);
        //If the object isn't falsy
        if (this.potentialObj) {
          //Set last object to evaluated object
          this.lastObj = this.potentialObj;
          //Set complete strings to all keys of object
          this.completeStrings = getProps(this.lastObj);
          this.completeStrings.forEach((v) => {
            return keyToDesc(this.lastObj, v);
          });
        }
      } catch (ex) {
        //No match
        console.warn(ex);
      }
    } else {
      //if (this.lastObj) {
      //Get the last bit after the last period
      this.prop = str.split(".").pop();

      //If we completed the word property return it by itself
      // if (this.lastObj && this.lastObj[this.prop]) {
      //   this.completeStrings.length = 1;
      //   this.completeStrings[0] = keyToDesc(this.lastObj, this.prop);
      // } else { //Else, try to complete the property be seeing if we have a part of it
      if (this.lastObj === undefined) {
        //If we don't have a last object and we're not referencing subobjects
        if (!str.includes(".")) {
          //Use global window object as reference for autocomplete on global
          this.lastObj = window;
        }
      }
      //If our last object reference is actually an object
      if (this.lastObj instanceof Object) {
        //Get its keys
        this.lastKeys = getProps(this.lastObj);//Object.keys(this.lastObj);
        //Clear our returned strings
        this.completeStrings.length = 0;
        //Loop through keys to see if we can match them with our search prop
        let matchkey;
        for (let key of this.lastKeys) {
          matchkey = key.toLowerCase(); //More matching!
          //if (key.startsWith(this.prop)) {
          if (matchkey.includes(this.prop.toLowerCase())) {
            key = keyToDesc(this.lastObj, key);
            this.completeStrings.push(key);
          }
        }
      }
      //}
      //}
    }
  }
}

let tabCompleter = new EvalComplete();

globalThis.onTabComplete = function (str) {
  tabCompleter.complete(str);
  return tabCompleter.completeStrings;
}
