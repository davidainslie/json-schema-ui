@app = angular.module("app", ["ngResource", "ngAnimate", "ui.router", "ui.bootstrap", "mgcrea.ngStrap", "xtForm", "toaster"])

@app.config ($stateProvider, $urlRouterProvider) ->
  $urlRouterProvider
    .otherwise("/")

  $stateProvider
    .state("home", {
      url: "/", templateUrl: "/assets/partials/dashboard.html"
    })
    .state("licence-applications-subscribe", {
      url: "/licence-applications-subscribe", templateUrl: "/assets/partials/licence-applications-subscribe.html"
    })
    .state("single-page", {
      url: "/single-page/*id", templateUrl: "/assets/partials/default-template.html" , controller: "DataController"
    })

String::replaceAll = (targetString, subString) ->
  inputString = this
  inputString = inputString.replace(new RegExp(targetString, "g"), subString)
  inputString

String::replaceAllCaseInSensitive = (targetString, subString) ->
  inputString = this
  inputString = inputString.replace(new RegExp(targetString, "gi"), subString) #replace a string globally and case-insensitive
  inputString