@app.directive "dependencies", ($compile) ->
  restrict: "A"
  link: (scope, element, attrs) ->
    fullKey = scope.property.id.replace(scope.jsonSchema.id, "").replace(/#/g, "")
    key = fullKey.substr(fullKey.lastIndexOf('/') + 1)

    path = scope.property.id.replace(scope.jsonSchema.id, "").replace(/#/g, "").split("/")
    path.splice(path.length - 1, 1, "dependencies")
    dependenciesPath = path.join("//")

    # One Of
    oneOfDependentValues = []

    for dependentDeclaration in JSON.search(scope.jsonSchema, dependenciesPath)
      for dependentKey of dependentDeclaration
        if key != dependentKey
          oneOfs = JSON.search(dependentDeclaration, "//#{dependentKey}//oneOf")

          if oneOfs.length > 0
            oneOfs.each (oneOf) ->
              if key in JSPath.apply(".required", oneOf)
                for property of oneOf["properties"] when property != key
                  dependentModel = "form." + scope.property.id.replace(scope.jsonSchema.id, "").replace(/#/g, "").substring(1).replace(/\//g, ".").replace(key, property)

                  for dependentValue in oneOf["properties"]["#{property}"]["enum"]
                    oneOfDependentValues.push "#{dependentModel} == '#{dependentValue}'"

    if oneOfDependentValues.length > 0
      console.debug "#{oneOfDependentValues.join(' || ')}"
      element.attr "data-ng-show", "#{oneOfDependentValues.join(' || ')}"

    element.removeAttr "dependencies"
    $compile(element) scope