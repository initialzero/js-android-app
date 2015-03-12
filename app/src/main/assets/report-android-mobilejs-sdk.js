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
  define('js.mobile.report.callback', [],function() {
    var ReportCallback;
    return ReportCallback = (function() {
      function ReportCallback() {}

      ReportCallback.prototype.onScriptLoaded = function() {};

      ReportCallback.prototype.onLoadStart = function() {};

      ReportCallback.prototype.onLoadDone = function(parameters) {};

      ReportCallback.prototype.onLoadError = function(error) {};

      ReportCallback.prototype.onTotalPagesLoaded = function(pages) {};

      ReportCallback.prototype.onPageChange = function(page) {};

      ReportCallback.prototype.onRemoteCall = function(type, location) {};

      return ReportCallback;

    })();
  });

}).call(this);

(function() {
  var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    hasProp = {}.hasOwnProperty;

  define('js.mobile.android.report.callback', ['require','js.mobile.report.callback'],function(require) {
    var Callback, ReportCallback;
    Callback = require('js.mobile.report.callback');
    return ReportCallback = (function(superClass) {
      extend(ReportCallback, superClass);

      function ReportCallback() {
        return ReportCallback.__super__.constructor.apply(this, arguments);
      }

      ReportCallback.prototype.onScriptLoaded = function() {
        Android.onScriptLoaded();
      };

      ReportCallback.prototype.onLoadStart = function() {
        Android.onLoadStart();
      };

      ReportCallback.prototype.onLoadDone = function(parameters) {
        Android.onLoadDone(parameters);
      };

      ReportCallback.prototype.onLoadError = function(error) {
        Android.onLoadError(error);
      };

      ReportCallback.prototype.onTotalPagesLoaded = function(pages) {
        Android.onTotalPagesLoaded(pages);
      };

      ReportCallback.prototype.onPageChange = function(page) {
        Android.onPageChange(page);
      };

      ReportCallback.prototype.onRemoteCall = function(type, location) {
        Android.onRemoteCall(type, location);
      };

      return ReportCallback;

    })(Callback);
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
  define('js.mobile.report.session', [],function() {
    var Session;
    return Session = (function() {
      function Session(options) {
        this.username = options.username, this.password = options.password, this.organization = options.organization;
      }

      Session.prototype.authOptions = function() {
        return {
          auth: {
            name: this.username,
            password: this.password,
            organization: this.organization
          }
        };
      };

      return Session;

    })();
  });

}).call(this);

(function() {
  var bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  define('js.mobile.report.controller', [],function() {
    var ReportController;
    return ReportController = (function() {
      function ReportController(options) {
        this._processErrors = bind(this._processErrors, this);
        this._processSuccess = bind(this._processSuccess, this);
        this._processChangeTotalPages = bind(this._processChangeTotalPages, this);
        this._processLinkClicks = bind(this._processLinkClicks, this);
        this._executeReport = bind(this._executeReport, this);
        this.context = options.context, this.session = options.session, this.uri = options.uri, this.params = options.params;
        this.callback = this.context.callback;
        this.logger = this.context.logger;
        this.logger.log(this.uri);
        this.params || (this.params = {});
        this.totalPages = 0;
      }

      ReportController.prototype.selectPage = function(page) {
        if (this.loader != null) {
          return this.loader.pages(page).run().done(this._processSuccess).fail(this._processErrors);
        }
      };

      ReportController.prototype.runReport = function() {
        this.callback.onLoadStart();
        this.logger.log("start loading visualize");
        return visualize(this.session.authOptions(), this._executeReport);
      };

      ReportController.prototype._executeReport = function(visualize) {
        this.logger.log("start report execution");
        return this.loader = visualize.report({
          resource: this.uri,
          params: this.params,
          container: "#container",
          scale: "width",
          linkOptions: {
            events: {
              click: this._processLinkClicks
            }
          },
          error: this._processErrors,
          events: {
            changeTotalPages: this._processChangeTotalPages
          },
          success: this._processSuccess
        });
      };

      ReportController.prototype._processLinkClicks = function(event, link) {};

      ReportController.prototype._processChangeTotalPages = function(totalPages) {
        this.totalPages = totalPages;
        return this.callback.onTotalPagesLoaded(this.totalPages);
      };

      ReportController.prototype._processSuccess = function(parameters) {
        this.logger.log(parameters);
        return this.callback.onLoadDone(parameters);
      };

      ReportController.prototype._processErrors = function(error) {
        this.logger.log(error);
        return this.callback.onLoadError(error);
      };

      return ReportController;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.report', ['require','js.mobile.report.session','js.mobile.report.controller'],function(require) {
    var MobileReport, ReportController, Session, root;
    Session = require('js.mobile.report.session');
    ReportController = require('js.mobile.report.controller');
    MobileReport = (function() {
      MobileReport._instance = null;

      MobileReport.getInstance = function(context) {
        return this._instance || (this._instance = new MobileReport(context));
      };

      function MobileReport(context1) {
        this.context = context1;
      }

      MobileReport.run = function(options) {
        return this._instance.run(options);
      };

      MobileReport.selectPage = function(page) {
        return this._instance.selectPage(page);
      };

      MobileReport.prototype.selectPage = function(page) {
        if (this.reportController) {
          return this.reportController.selectPage(page);
        }
      };

      MobileReport.prototype.run = function(options) {
        options.session = new Session(options);
        options.context = this.context;
        this.reportController = new ReportController(options);
        return this.reportController.runReport();
      };

      return MobileReport;

    })();
    root = typeof window !== "undefined" && window !== null ? window : exports;
    return root.MobileReport = MobileReport;
  });

}).call(this);

(function() {
  var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    hasProp = {}.hasOwnProperty;

  define('js.mobile.android.report.client', ['require','js.mobile.client','js.mobile.android.report.callback','js.mobile.android.logger','js.mobile.context','js.mobile.report'],function(require) {
    var AndroidLogger, Context, MobileClient, MobileReport, ReportCallback, ReportClient;
    MobileClient = require('js.mobile.client');
    ReportCallback = require('js.mobile.android.report.callback');
    AndroidLogger = require('js.mobile.android.logger');
    Context = require('js.mobile.context');
    MobileReport = require('js.mobile.report');
    return ReportClient = (function(superClass) {
      extend(ReportClient, superClass);

      function ReportClient() {
        return ReportClient.__super__.constructor.apply(this, arguments);
      }

      ReportClient.prototype.run = function() {
        var callbackImplementor, context, logger;
        callbackImplementor = new ReportCallback();
        logger = new AndroidLogger();
        context = new Context({
          callback: callbackImplementor,
          logger: logger
        });
        MobileReport.getInstance(context);
        return callbackImplementor.onScriptLoaded();
      };

      return ReportClient;

    })(MobileClient);
  });

}).call(this);

(function() {
  require(['js.mobile.android.report.client'], function(ReportClient) {
    return new ReportClient().run();
  });

}).call(this);

define("android/report/main.js", function(){});

