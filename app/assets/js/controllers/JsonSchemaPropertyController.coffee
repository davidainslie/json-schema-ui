@app.controller "JsonSchemaPropertyController", ($rootScope, $scope) ->
  $scope.property = $scope.jsonSchemaProperty

  id = $scope.property.id

  if id
    $scope.input = id.replace($scope.jsonSchema.id, "").replace(/#/g, "").substring(1).replace(/\//g, ".").replace(/\./g, "")

    $scope.required = (->
      fullKey = id.replace($scope.jsonSchema.id, "").replace(/#/g, "")
      key = fullKey.substr(fullKey.lastIndexOf('/') + 1)
      parentKey = fullKey.substr(0, fullKey.lastIndexOf('/')).replace(/\//g, "//")

      required = JSON.search($scope.jsonSchema, parentKey + "/required")

      required && required.length > 0 && key in required)()