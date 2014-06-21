@app.controller "LicenceApplicationsSubscribeController", ($log, $scope) ->
  $log.info "Subscribing to new licence applications"

  $scope.socket = new WebSocket("ws://localhost:9000/api/licence-applications/subscribe/all")

  $scope.socket.open = (event) ->
    $log.info "Websocket open: #{event}"

  $scope.socket.onmessage = (event) ->
    $log.info "Websocket onmessage: #{event.data}"
    $scope.licenceApplication = JSON.stringify(JSON.parse(event.data), null, 2)
    $scope.$apply()