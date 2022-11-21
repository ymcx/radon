(() => {
    const pageScript = () => {
      const hiddenCSS = {
        "m.youtube.com": [
          "ytm-pivot-bar-item-renderer:nth-of-type(2)",
          "ytm-channel-list-sub-menu-renderer",
          ".companion-ad-container",
          ".ytp-ad-action-interstitial",
          "ytm-companion-ad-renderer",
          "ytm-companion-slot",
          "ytm-promoted-sparkles-web-renderer",
          "ytm-promoted-video-renderer"
        ]
      };
      const hideElements = (hostname) => {
        const selectors = hiddenCSS[hostname];
        const style = document.createElement("style");
        style.innerHTML = `${selectors.join(", ")} {display:none !important;} \n body {-webkit-tap-highlight-color:transparent !important;}`;
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
      //jsonOverride("playerAds", []);
      hideElements(window.location.hostname);
      hideDynamicAds();
    };
    const script = document.createElement("script");
    const scriptText = pageScript.toString();
    script.innerHTML = `(${scriptText})();`;
    document.head.appendChild(script);
    document.head.removeChild(script);
})();
