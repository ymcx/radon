(() => {
  function runBlockYoutube() {
    const allowedHostnames = [
      "m.youtube.com"
    ];
    if (!allowedHostnames.includes(window.location.hostname)) {
      return {
        success: false,
        status: "wrongDomain",
        message: "wrongDomain"
      };
    }
    const pageScript = () => {
      const hiddenCSS = {
        "m.youtube.com": [
          ".chips-visible", //list of interests in videos
          "ytm-pivot-bar-item-renderer:nth-of-type(2)", // shorts tab
          ".playlist-panel-header-byline", // name of the creator of the playlist 
          "ytm-playlist-controls", // next and previous video buttons in a playlist
          ".center.player-controls-middle > button.icon-button:nth-of-type(1)", // previous video button
          ".center.player-controls-middle > button.icon-button:nth-of-type(5)", // next video button
          ".ytm-autonav-toggle-button-container", // up next text
          ".ytm-autonav-bar", // autoplay button
          ".rich-grid-sticky-header", // list of interests in home
          "ytm-channel-list-sub-menu-renderer", // list of subscriptions in subscriptions 
          ".companion-ad-container",
          ".ytp-ad-action-interstitial",
          '.ytp-cued-thumbnail-overlay > div[style*="/sddefault.jpg"]',
          `a[href^="/watch?v="][onclick^="return koya.onEvent(arguments[0]||window.event,'"]:not([role]):not([class]):not([id])`,
          `a[onclick*='"ping_url":"http://www.google.com/aclk?']`,
          "ytm-companion-ad-renderer",
          "ytm-companion-slot",
          "ytm-promoted-sparkles-text-search-renderer",
          "ytm-promoted-sparkles-web-renderer",
          "ytm-promoted-video-renderer"
        ]
      };
      const hideElements = (hostname) => {
        const selectors = hiddenCSS[hostname];
        if (!selectors) {
          return;
        }
        const rule = `${selectors.join(", ")} {display:none !important;} \n body {-webkit-tap-highlight-color:transparent !important;}`;
        const style = document.createElement("style");
        style.innerHTML = rule;
        document.head.appendChild(style);
      };
      const hideDynamicAds = () => {
        const elements = document.querySelectorAll("#contents > ytd-rich-item-renderer ytd-display-ad-renderer");
        if (elements.length === 0) {
          return;
        }
        elements.forEach((el) => {
          if (el.parentNode && el.parentNode.parentNode) {
            const parent = el.parentNode.parentNode;
            if (parent.localName === "ytd-rich-item-renderer") {
              parent.style.display = "none";
            }
          }
        });
      };
      const overrideObject = (obj, propertyName, overrideValue) => {
        if (!obj) {
          return false;
        }
        let overriden = false;
        for (const key in obj) {
          if (obj.hasOwnProperty(key) && key === propertyName) {
            obj[key] = overrideValue;
            overriden = true;
          } else if (obj.hasOwnProperty(key) && typeof obj[key] === "object") {
            if (overrideObject(obj[key], propertyName, overrideValue)) {
              overriden = true;
            }
          }
        }
        if (overriden) {
          console.log(`found: ${propertyName}`);
        }
        return overriden;
      };
      const jsonOverride = (propertyName, overrideValue) => {
        const nativeJSONParse = JSON.parse;
        JSON.parse = (...args) => {
          const obj = nativeJSONParse.apply(this, args);
          overrideObject(obj, propertyName, overrideValue);
          return obj;
        };
        const nativeResponseJson = Response.prototype.json;
        Response.prototype.json = new Proxy(nativeResponseJson, {
          apply(...args) {
            const promise = Reflect.apply(args);
            return new Promise((resolve, reject) => {
              promise.then((data) => {
                overrideObject(data, propertyName, overrideValue);
                resolve(data);
              }).catch((error) => reject(error));
            });
          }
        });
      };
      jsonOverride("adPlacements", []);
      jsonOverride("playerAds", []);
      hideElements(window.location.hostname);
      hideDynamicAds();
    };
    const script = document.createElement("script");
    const scriptText = pageScript.toString();
    script.innerHTML = `(${scriptText})();`;
    document.head.appendChild(script);
    document.head.removeChild(script);
    return {
      success: true,
      status: "success",
      message: "success"
    };
  }
  (() => {
    let finish = (m) => {
      console.log(m);
    };
    if (typeof completion !== "undefined") {
      finish = completion;
    }
    try {
      const result = runBlockYoutube();
      finish(result.message);
    } catch (ex) {
      finish(ex.toString());
    }
  })();
})();
