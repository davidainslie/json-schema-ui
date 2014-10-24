@app.controller "DataSubscribeController", ($log, $scope) ->
  $log.info "Subscribing to new data"

  $scope.socket = new WebSocket("ws://localhost:9000/api/data/subscribe/all")

  $scope.socket.open = (event) ->
    $log.info "Websocket open: #{event}"

  $scope.socket.onmessage = (event) ->
    $log.info "Websocket onmessage: #{event.data}"
    $scope.data = JSON.stringify(JSON.parse(event.data), null, 2)
    $scope.$apply()