@app.controller "DataController", ($rootScope, $scope, $modal, $stateParams, toaster, Scroller, Data) ->
  $scope.data = {}

  console.log $stateParams.id

  Data.schema($stateParams.id).then(
    (success) ->
      console.info success
      $scope.jsonSchema = angular.fromJson(success)
      $scope.data.id = $scope.jsonSchema.id

    (exception) ->
      console.error exception
      $modal({title: "Submission Error", content: "#{$scope.jsonSchema.id}: #{exception.statusText}", animation: "am-fade-and-scale"}))

  $scope.submit = ->
    if ($scope.form.$valid)
      Data.save(removeNulls($scope.data)).then(
        (success) ->
          console.info success
          Scroller.scrollTo "form"

          $scope.data = {}
          $scope.data.id = $scope.jsonSchema.id

          $scope.form.$setPristine()
          $modal({title: "Successful Submission", content: success.response, animation: "am-fade-and-scale"})

        (exception) ->
          console.error exception
          Scroller.scrollTo "form"
          $modal({title: "Failed Submission", content: exception.data.response, animation: "am-fade-and-scale"}))
    else
      notToSubmit = angular.toJson($scope.form)
      console.log "NOT Submitting: #{notToSubmit}"

      angular.forEach $("#form").find(".ng-invalid"), (node) ->
        angular.element(document.getElementById("#{node.id}")).removeClass("ng-pristine").addClass("ng-dirty").addClass("xt-error")

      Scroller.scrollToLabel($("#form").find(".ng-invalid")[0].getAttribute('id'))
      toaster.pop('error', "Submission Failure", "Amend errors (including required missing data).", 5000, 'trustedHtml')

  $scope.note = (id) ->
    (note for note in $scope.view.notes when note.id is id)[0]

  $scope.propertiesOf = (json) ->
    (property for property in JSPath.apply('.properties.*', json)) if json

  removeNulls = (obj) ->
    isArray = obj instanceof Array

    for k of obj
      if obj[k] is null
        (if isArray then obj.splice(k, 1) else delete obj[k])
      else removeNulls obj[k] if typeof obj[k] is "object"

    obj