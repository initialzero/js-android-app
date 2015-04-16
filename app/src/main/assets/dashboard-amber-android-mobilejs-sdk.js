(function() {
  define('js.mobile.android.dashboard.callback', ['require'],function(require) {
    var AndroidCallback;
    return AndroidCallback = (function() {
      function AndroidCallback() {}

      AndroidCallback.prototype.onMaximizeStart = function(title) {
        Android.onMaximizeStart(title);
      };

      AndroidCallback.prototype.onMaximizeEnd = function(title) {
        Android.onMaximizeEnd(title);
      };

      AndroidCallback.prototype.onMaximizeFailed = function(error) {
        Android.onMaximizeFailed(error);
      };

      AndroidCallback.prototype.onMinimizeStart = function() {
        Android.onMinimizeStart();
      };

      AndroidCallback.prototype.onMinimizeEnd = function() {
        Android.onMinimizeEnd();
      };

      AndroidCallback.prototype.onMinimizeFailed = function(error) {
        Android.onMinimizeFailed(error);
      };

      AndroidCallback.prototype.onScriptLoaded = function() {
        Android.onScriptLoaded();
      };

      AndroidCallback.prototype.onLoadStart = function() {
        Android.onLoadStart();
      };

      AndroidCallback.prototype.onLoadDone = function(components) {
        Android.onLoadDone();
      };

      AndroidCallback.prototype.onLoadError = function(error) {
        Android.onLoadError(error);
      };

      AndroidCallback.prototype.onReportExecution = function(data) {
        Android.onReportExecution(data);
      };

      return AndroidCallback;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.logger', [],function() {
    var Logger;
    return Logger = (function() {
      function Logger() {}

      Logger.prototype.log = function(message) {};

      return Logger;

    })();
  });

}).call(this);

(function() {
  var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    hasProp = {}.hasOwnProperty;

  define('js.mobile.android.logger', ['js.mobile.logger'], function(Logger) {
    var AndroidLogger;
    return AndroidLogger = (function(superClass) {
      extend(AndroidLogger, superClass);

      function AndroidLogger() {
        return AndroidLogger.__super__.constructor.apply(this, arguments);
      }

      AndroidLogger.prototype.log = function(message) {
        return console.log(message);
      };

      return AndroidLogger;

    })(Logger);
  });

}).call(this);

(function() {
  define('js.mobile.context', [],function() {
    var Context;
    return Context = (function() {
      function Context(options) {
        this.logger = options.logger, this.callback = options.callback;
      }

      Context.prototype.setWindow = function(window) {
        this.window = window;
      };

      return Context;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.amber.dashboard.view', [],function() {
    var View;
    return View = (function() {
      function View(options) {
        this.context = options.context, this.el = options.el;
        this.logger = this.context.logger;
      }

      View.prototype.scaleView = function() {
        var windowHeight, windowWidth;
        windowWidth = this.context.window.width;
        windowHeight = this.context.window.height;
        return this.setSize(windowWidth, windowHeight);
      };

      View.prototype.setSize = function(width, height) {
        this.logger.log("Set size. Width: " + width + ". Height: " + height);
        this.el.css('width', width);
        return this.el.css('height', height);
      };

      View.prototype.disable = function() {
        return this._setInteractive(false);
      };

      View.prototype.enable = function() {
        return this._setInteractive(true);
      };

      View.prototype._setInteractive = function(enable) {
        var pointerMode;
        pointerMode = enable ? "auto" : "none";
        this.logger.log("Toggle interaction: " + pointerMode);
        return this.el.css("pointer-events", pointerMode);
      };

      return View;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.amber.dashboard.controller', ['require','js.mobile.amber.dashboard.view'],function(require) {
    var DashboardController, View;
    View = require('js.mobile.amber.dashboard.view');
    return DashboardController = (function() {
      function DashboardController(context, viewport) {
        this.context = context;
        this.viewport = viewport;
        this.logger = this.context.logger;
        this.callback = this.context.callback;
        this.container = new View({
          el: jQuery('#frame'),
          context: this.context
        });
      }

      DashboardController.prototype.initialize = function() {
        this.callback.onLoadStart();
        this._removeRedundantArtifacts();
        this._injectViewport();
        return this._attachDashletLoadListeners();
      };

      DashboardController.prototype.minimizeDashlet = function() {
        this.logger.log("minimize dashlet");
        this.logger.log("Remove original scale");
        jQuery(".dashboardCanvas > .content > .body div.canvasOverlay").removeClass("originalDashletInScaledCanvas");
        jQuery("div.dashboardCanvas > div.content > div.body > div").find(".minimizeDashlet")[0].click();
        this._disableDashlets();
        return this.callback.onMinimizeStart();
      };

      DashboardController.prototype._injectViewport = function() {
        return this.viewport.configure();
      };

      DashboardController.prototype._scaleDashboard = function() {
        return jQuery('.dashboardCanvas').addClass('scaledCanvas');
      };

      DashboardController.prototype._attachDashletLoadListeners = function() {
        var timeInterval;
        return timeInterval = window.setInterval((function(_this) {
          return function() {
            var timeIntervalDashletContent;
            window.clearInterval(timeInterval);
            return timeIntervalDashletContent = window.setInterval(function() {
              var dashletContent, dashlets;
              dashlets = jQuery('.dashlet');
              if (dashlets.length > 0) {
                dashletContent = jQuery('.dashletContent > div.content');
                if (dashletContent.length === dashlets.length) {
                  _this._configureDashboard();
                  return window.clearInterval(timeIntervalDashletContent);
                }
              }
            }, 100);
          };
        })(this), 100);
      };

      DashboardController.prototype._configureDashboard = function() {
        this._createCustomOverlays();
        this._scaleDashboard();
        this._overrideDashletTouches();
        this._disableDashlets();
        return this.callback.onLoadDone();
      };

      DashboardController.prototype._removeRedundantArtifacts = function() {
        var customStyle;
        this.logger.log("remove artifacts");
        customStyle = ".header, .dashletToolbar, .show_chartTypeSelector_wrapper { display: none !important; } .column.decorated { margin: 0 !important; border: none !important; } .dashboardViewer.dashboardContainer>.content>.body, .column.decorated>.content>.body, .column>.content>.body { top: 0 !important; } #mainNavigation{ display: none !important; } .customOverlay { position: absolute; width: 100%; height: 100%; z-index: 1000; }";
        return jQuery('<style id="custom_mobile"></style').text(customStyle).appendTo('head');
      };

      DashboardController.prototype._createCustomOverlays = function() {
        var dashletElements;
        dashletElements = jQuery('.dashlet').not(jQuery('.inputControlWrapper').parentsUntil('.dashlet').parent());
        return jQuery.each(dashletElements, function(key, value) {
          var dashlet, overlay;
          dashlet = jQuery(value);
          overlay = jQuery("<div></div>");
          overlay.addClass("customOverlay");
          return dashlet.prepend(overlay);
        });
      };

      DashboardController.prototype._disableDashlets = function() {
        this.logger.log("disable dashlet touches");
        return jQuery('.customOverlay').css('display', 'block');
      };

      DashboardController.prototype._enableDashlets = function() {
        this.logger.log("enable dashlet touches");
        return jQuery('.customOverlay').css('display', 'none');
      };

      DashboardController.prototype._overrideDashletTouches = function() {
        var dashlets, self;
        this.logger.log("override dashlet touches");
        dashlets = jQuery('.customOverlay');
        dashlets.unbind();
        self = this;
        return dashlets.click(function() {
          var dashlet, innerLabel, title;
          dashlet = jQuery(this).parent();
          innerLabel = dashlet.find('.innerLabel > p');
          if ((innerLabel != null) && (innerLabel.text != null)) {
            title = innerLabel.text();
            if ((title != null) && title.length > 0) {
              return self._maximizeDashlet(dashlet, title);
            }
          }
        });
      };

      DashboardController.prototype._maximizeDashlet = function(dashlet, title) {
        var button;
        this.logger.log("maximizing dashlet");
        this._enableDashlets();
        this.callback.onMaximizeStart(title);
        button = jQuery(jQuery(dashlet).find('div.dashletToolbar > div.content div.buttons > .maximizeDashletButton')[0]);
        button.click();
        this.logger.log("Add original scale");
        return jQuery(".dashboardCanvas > .content > .body div.canvasOverlay").addClass("originalDashletInScaledCanvas");
      };

      return DashboardController;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.amber.dashboard.window', [],function() {
    var DashboardWindow;
    return DashboardWindow = (function() {
      function DashboardWindow(width, height) {
        this.width = width;
        this.height = height;
      }

      return DashboardWindow;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.amber.dashboard', ['require','js.mobile.amber.dashboard.controller','js.mobile.amber.dashboard.window'],function(require) {
    var DashboardController, DashboardWindow, MobileDashboard, root;
    DashboardController = require('js.mobile.amber.dashboard.controller');
    DashboardWindow = require('js.mobile.amber.dashboard.window');
    MobileDashboard = (function() {
      MobileDashboard._instance = null;

      MobileDashboard.getInstance = function(context, viewport) {
        return this._instance || (this._instance = new MobileDashboard(context, viewport));
      };

      MobileDashboard.run = function() {
        return this._instance.run();
      };

      MobileDashboard.minimizeDashlet = function() {
        return this._instance.minimizeDashlet();
      };

      function MobileDashboard(context1, viewport1) {
        this.context = context1;
        this.viewport = viewport1;
        this.context.callback.onScriptLoaded();
      }

      MobileDashboard.prototype.run = function() {
        var window;
        window = new DashboardWindow('100%', '100%');
        this.context.setWindow(window);
        this.dashboardController = new DashboardController(this.context, this.viewport);
        return this.dashboardController.initialize();
      };

      MobileDashboard.prototype.minimizeDashlet = function() {
        return this.dashboardController.minimizeDashlet();
      };

      return MobileDashboard;

    })();
    root = typeof window !== "undefined" && window !== null ? window : exports;
    return root.MobileDashboard = MobileDashboard;
  });

}).call(this);

(function() {
  define('js.mobile.android.viewport.dashboard.amber', [],function() {
    var Viewport;
    return Viewport = (function() {
      function Viewport() {}

      Viewport.prototype.configure = function() {
        var viewPort;
        viewPort = document.querySelector('meta[name=viewport]');
        return viewPort.setAttribute('content', "target-densitydpi=device-dpi, height=device-height, width=device-width, user-scalable=yes");
      };

      return Viewport;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.amber.android.dashboard.client', ['require','js.mobile.android.dashboard.callback','js.mobile.android.logger','js.mobile.context','js.mobile.amber.dashboard','js.mobile.android.viewport.dashboard.amber'],function(require) {
    var AndroidCallback, AndroidClient, AndroidLogger, Context, MobileDashboard, Viewport;
    AndroidCallback = require('js.mobile.android.dashboard.callback');
    AndroidLogger = require('js.mobile.android.logger');
    Context = require('js.mobile.context');
    MobileDashboard = require('js.mobile.amber.dashboard');
    Viewport = require('js.mobile.android.viewport.dashboard.amber');
    return AndroidClient = (function() {
      function AndroidClient() {}

      AndroidClient.prototype.run = function() {
        var context, viewport;
        context = new Context({
          callback: new AndroidCallback(),
          logger: new AndroidLogger()
        });
        viewport = new Viewport();
        MobileDashboard.getInstance(context, viewport);
        return MobileDashboard.run();
      };

      return AndroidClient;

    })();
  });

}).call(this);

(function() {
  require(['js.mobile.amber.android.dashboard.client'], function(AndroidClient) {
    return (function($) {
      return new AndroidClient().run();
    })(jQuery);
  });

}).call(this);

define("android/amber/dashboard/main.js", function(){});

