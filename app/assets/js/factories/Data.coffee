@app.factory "Data", ($resource) ->
  schema: (id) ->
    request = $resource("/api/data/#{id}")
    request.get().$promise

  save: (data) ->
    console.debug "Data.save: #{angular.toJson(data, true)}"

    request = $resource("/api/data")
    request.save(data).$promise