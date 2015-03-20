(function() {
  define('js.mobile.android.report.callback', ['require'],function(require) {
    var ReportCallback;
    return ReportCallback = (function() {
      function ReportCallback() {}

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

      ReportCallback.prototype.onReferenceClick = function(location) {
        Android.onReferenceClick(location);
      };

      ReportCallback.prototype.onReportExecutionClick = function(reportUri, params) {
        Android.onReportExecutionClick(reportUri, params);
      };

      return ReportCallback;

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
  define('js.mobile.session', [],function() {
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
        this._notifyPageChange = bind(this._notifyPageChange, this);
        this._openRemoteLink = bind(this._openRemoteLink, this);
        this._navigateToPage = bind(this._navigateToPage, this);
        this._navigateToAnchor = bind(this._navigateToAnchor, this);
        this._startReportExecution = bind(this._startReportExecution, this);
        this._processLinkClicks = bind(this._processLinkClicks, this);
        this._processErrors = bind(this._processErrors, this);
        this._processSuccess = bind(this._processSuccess, this);
        this._processChangeTotalPages = bind(this._processChangeTotalPages, this);
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
        return visualize(this.session.authOptions(), this._executeReport);
      };

      ReportController.prototype._executeReport = function(visualize) {
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

      ReportController.prototype._processChangeTotalPages = function(totalPages) {
        this.totalPages = totalPages;
        return this.callback.onTotalPagesLoaded(this.totalPages);
      };

      ReportController.prototype._processSuccess = function(parameters) {
        return this.callback.onLoadDone(parameters);
      };

      ReportController.prototype._processErrors = function(error) {
        this.logger.log(error);
        return this.callback.onLoadError(error);
      };

      ReportController.prototype._processLinkClicks = function(event, link) {
        var type;
        type = link.type;
        switch (type) {
          case "ReportExecution":
            return this._startReportExecution(link);
          case "LocalAnchor":
            return this._navigateToAnchor(link);
          case "LocalPage":
            return this._navigateToPage(link);
          case "Reference":
            return this._openRemoteLink(link);
        }
      };

      ReportController.prototype._startReportExecution = function(link) {
        var params, paramsAsString, reportUri;
        params = link.parameters;
        reportUri = params._report;
        paramsAsString = JSON.stringify(params, null, 2);
        return this.callback.onReportExecutionClick(reportUri, paramsAsString);
      };

      ReportController.prototype._navigateToAnchor = function(link) {
        return window.location.hash = link.href;
      };

      ReportController.prototype._navigateToPage = function(link) {
        var href, matches, numberPattern, pageNumber;
        href = link.href;
        numberPattern = /\d+/g;
        matches = href.match(numberPattern);
        if (matches != null) {
          pageNumber = matches.join("");
          return this._loadPage(pageNumber);
        }
      };

      ReportController.prototype._openRemoteLink = function(link) {
        var href;
        href = link.href;
        return this.callback.onReferenceClick(href);
      };

      ReportController.prototype._loadPage = function(page) {
        return this.loader.pages(page).run().fail(this._processErrors).done(this._notifyPageChange);
      };

      ReportController.prototype._notifyPageChange = function() {
        return this.callback.onPageChange(this.loader.pages());
      };

      return ReportController;

    })();
  });

}).call(this);

(function() {
  define('js.mobile.report', ['require','js.mobile.session','js.mobile.report.controller'],function(require) {
    var MobileReport, ReportController, Session, root;
    Session = require('js.mobile.session');
    ReportController = require('js.mobile.report.controller');
    MobileReport = (function() {
      MobileReport._instance = null;

      MobileReport.getInstance = function(context) {
        return this._instance || (this._instance = new MobileReport(context));
      };

      function MobileReport(context1) {
        this.context = context1;
        this.context.callback.onScriptLoaded();
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
  define('js.mobile.android.report.client', ['require','js.mobile.android.report.callback','js.mobile.android.logger','js.mobile.context','js.mobile.report'],function(require) {
    var AndroidLogger, Context, MobileReport, ReportCallback, ReportClient;
    ReportCallback = require('js.mobile.android.report.callback');
    AndroidLogger = require('js.mobile.android.logger');
    Context = require('js.mobile.context');
    MobileReport = require('js.mobile.report');
    return ReportClient = (function() {
      function ReportClient() {}

      ReportClient.prototype.run = function() {
        var context;
        context = new Context({
          callback: new ReportCallback(),
          logger: new AndroidLogger()
        });
        return MobileReport.getInstance(context);
      };

      return ReportClient;

    })();
  });

}).call(this);

(function() {
  require(['js.mobile.android.report.client'], function(ReportClient) {
    return new ReportClient().run();
  });

}).call(this);

define("android/report/main.js", function(){});

