@app.factory "Scroller", ->
  scrollTo: (id) ->
    idWithoutIndex = id.replace /-\d+/, ""
    target = $("#" + idWithoutIndex)
    $("body,html").animate({scrollTop: target.offset().top}, "slow")

  scrollToLabel: (id) -> @scrollTo "#{id}Label"