(function() {
  define('js.mobile.client', [],function() {
    var Client;
    return Client = (function() {
      function Client() {}

      return Client;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.callback.implementor', [],function() {
    var CallbackImplementor;
    return CallbackImplementor = (function() {
      function CallbackImplementor() {}

      CallbackImplementor.prototype.onMaximize = function(title) {};

      CallbackImplementor.prototype.onMinimize = function() {};

      CallbackImplementor.prototype.onWrapperLoaded = function() {};

      CallbackImplementor.prototype.onDashletsLoaded = function() {};

      return CallbackImplementor;

    })();
  });

}).call(this);

(function() {
  var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    hasProp = {}.hasOwnProperty;

  define('js.mobile.android.callback.implementor', ['js.mobile.callback.implementor'], function(CallbackImplementor) {
    var AndroidCallbackImplementor;
    return AndroidCallbackImplementor = (function(superClass) {
      extend(AndroidCallbackImplementor, superClass);

      function AndroidCallbackImplementor() {
        return AndroidCallbackImplementor.__super__.constructor.apply(this, arguments);
      }

      AndroidCallbackImplementor.prototype.onMaximize = function(title) {
        Android.onMaximize(title);
      };

      AndroidCallbackImplementor.prototype.onMinimize = function() {
        Android.onMinimize();
      };

      AndroidCallbackImplementor.prototype.onWrapperLoaded = function() {
        Android.onWrapperLoaded();
      };

      AndroidCallbackImplementor.prototype.onDashletsLoaded = function() {
        Android.onDashletsLoaded();
      };

      return AndroidCallbackImplementor;

    })(CallbackImplementor);
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
  define('js.mobile.view', [],function() {
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
  define('js.mobile.dashboard.controller', ['js.mobile.view'], function(View) {
    var DashboardController;
    return DashboardController = (function() {
      function DashboardController(context) {
        this.context = context;
        this.logger = this.context.logger;
        this.callback = this.context.callback;
        this.container = new View({
          el: jQuery('#frame'),
          context: this.context
        });
        this.dashletsLoaded = false;
      }

      DashboardController.prototype.initialize = function() {
        this._injectViewport();
        this._scaleDashboard();
        return this._attachDashletLoadListeners();
      };

      DashboardController.prototype.minimizeDashlet = function() {
        this.logger.log("minimize dashlet");
        jQuery("div.dashboardCanvas > div.content > div.body > div").find(".minimizeDashlet")[0].click();
        this._disableDashlets();
        return this.callback.onMinimize();
      };

      DashboardController.prototype._injectViewport = function() {
        var viewPort;
        viewPort = document.querySelector('meta[name=viewport]');
        return viewPort.setAttribute('content', "width=device-width, height=device-height, user-scalable=yes");
      };

      DashboardController.prototype._scaleDashboard = function() {
        return this.container.scaleView();
      };

      DashboardController.prototype._attachDashletLoadListeners = function() {
        var self;
        self = this;
        return jQuery(document).bind('DOMNodeInserted', function(e) {
          var dashletContent, dashlets;
          dashlets = jQuery('.dashlet');
          self._removeRedundantArtifacts();
          if (dashlets.length > 0) {
            dashletContent = jQuery('.dashletContent > div.content');
            if (dashletContent.length === dashlets.length && !self.dashletsLoaded) {
              self.dashletsLoaded = true;
              self._configureDashboard();
            }
          }
        });
      };

      DashboardController.prototype._configureDashboard = function() {
        this._overrideDashletTouches();
        this._disableDashlets();
        this._removeRedundantArtifacts();
        return this.callback.onDashletsLoaded();
      };

      DashboardController.prototype._removeRedundantArtifacts = function() {
        jQuery('.header').hide();
        jQuery('.dashletToolbar').first().hide();
        jQuery('.show_chartTypeSelector_wrapper').hide();
        jQuery('.column.decorated').css('margin', '0px');
        jQuery('.column.decorated').css('border', 'none');
        jQuery('.dashboardViewer .dashboardContainer > .content > .body').css('top', '0px');
        jQuery('.column.decorated > .content > .body').css('top', '0px');
        jQuery('.column > .content > .body').css('top', '0px');
        jQuery('body').css('-webkit-transform', 'translateZ(0) !important');
        return jQuery('body').css('-webkit-backface-visibility', 'hidden !important');
      };

      DashboardController.prototype._disableDashlets = function() {
        var dashletElements, dashlets;
        this.logger.log("disable dashlet touches");
        dashletElements = jQuery('.dashlet').not(jQuery('.inputControlWrapper').parentsUntil('.dashlet').parent());
        dashlets = new View({
          el: dashletElements,
          context: this.context
        });
        return dashlets.disable();
      };

      DashboardController.prototype._overrideDashletTouches = function() {
        var dashlets, self;
        this.logger.log("override dashlet touches");
        dashlets = jQuery('div.dashboardCanvas > div.content > div.body > div');
        dashlets.unbind();
        self = this;
        return dashlets.click(function() {
          var dashlet, innerLabel, title;
          dashlet = jQuery(this);
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
        var button, dashletElements, dashlets;
        this.logger.log("maximizing dashlet");
        this.logger.log("context: " + this.context);
        dashletElements = jQuery('.dashlet').not(jQuery('.inputControlWrapper').parentsUntil('.dashlet').parent());
        dashlets = new View({
          el: dashletElements,
          context: this.context
        });
        dashlets.enable();
        this.callback.onMaximize(title);
        button = jQuery(jQuery(dashlet).find('div.dashletToolbar > div.content div.buttons > .maximizeDashletButton')[0]);
        return button.click();
      };

      return DashboardController;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.dashboard.window', [],function() {
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
  define('js.mobile.dashboard.wrapper', ['js.mobile.dashboard.controller', 'js.mobile.dashboard.window'], function(DashboardController, DashboardWindow) {
    var DashboardWrapper, root;
    DashboardWrapper = (function() {
      DashboardWrapper._instance = null;

      DashboardWrapper.getInstance = function(context) {
        return this._instance || (this._instance = new DashboardWrapper(context));
      };

      DashboardWrapper.wrapScreen = function(width, height) {
        return this._instance.wrapScreen(width, height);
      };

      DashboardWrapper.minimizeDashlet = function() {
        return this._instance.minimizeDashlet();
      };

      function DashboardWrapper(context1) {
        this.context = context1;
        this.context.callback.onDashletsLoaded();
      }

      DashboardWrapper.prototype.wrapScreen = function(width, height) {
        var window;
        window = new DashboardWindow(width, height);
        this.context.setWindow(window);
        this.dashboardController = new DashboardController(this.context);
        return this.dashboardController.initialize();
      };

      DashboardWrapper.prototype.minimizeDashlet = function() {
        return this.dashboardController.minimizeDashlet();
      };

      return DashboardWrapper;

    })();
    root = typeof window !== "undefined" && window !== null ? window : exports;
    return root.DashboardWrapper = DashboardWrapper;
  });

}).call(this);

(function() {
  var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    hasProp = {}.hasOwnProperty;

  define('js.mobile.android.client', ['js.mobile.client', 'js.mobile.android.callback.implementor', 'js.mobile.android.logger', 'js.mobile.context', 'js.mobile.dashboard.wrapper'], function(MobileClient, AndroidCallbackImplementor, AndroidLogger, Context, DashboardWrapper) {
    var AndroidClient;
    return AndroidClient = (function(superClass) {
      extend(AndroidClient, superClass);

      function AndroidClient() {
        return AndroidClient.__super__.constructor.apply(this, arguments);
      }

      AndroidClient.prototype.run = function() {
        var callbackImplementor, context, logger;
        callbackImplementor = new AndroidCallbackImplementor();
        logger = new AndroidLogger();
        context = new Context({
          callback: callbackImplementor,
          logger: logger
        });
        DashboardWrapper.getInstance(context);
        return callbackImplementor.onWrapperLoaded();
      };

      return AndroidClient;

    })(MobileClient);
  });

}).call(this);

(function() {
  require(['js.mobile.android.client'], function(AndroidClient) {
    return (function($) {
      return new AndroidClient().run();
    })(jQuery);
  });

}).call(this);

define("android/main.js", function(){});

