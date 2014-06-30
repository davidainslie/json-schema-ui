@app.directive "component", ->
  restrict: "A"

  require: "ngModel"

  link: (scope, element, attrs, controller) ->
    element.removeAttr "component"

    encodedComponentModel = (error) -> (scope.component.model.replace /\./g, "-") + "-#{error}"

    isDirty = -> element.hasClass("ng-dirty")

    invalidityCount = 0

    show = (id) ->
      $("#" + id).show()
      element.addClass "invalid-input" if invalidityCount == 0
      invalidityCount = invalidityCount + 1

    hide = (id) ->
      $("#" + id).hide()
      invalidityCount = invalidityCount - 1
      invalidityCount = 0 if invalidityCount < 0
      element.removeClass "invalid-input" if invalidityCount == 0

    # Append "error" components to this component
    scope.inputCtrl = controller

    do requiredError = ->
      error = encodedComponentModel "requiredError"

      element.after """
        <small id="#{error}" class="help-block invalid-text">#{scope.component.label} is required</small>
      """ if scope.component.validations?.required

      scope.$watch "inputCtrl.$error.required", (invalidity) -> if (invalidity and isDirty()) then show(error) else hide(error)

    do minlengthError = ->
      error = encodedComponentModel "minlengthError"

      element.after """
        <small id="#{error}" class="help-block invalid-text">#{scope.component.label} must be at least #{scope.component.validations.minLength} characters</small>
      """ if scope.component.validations?.minLength?

      scope.$watch "inputCtrl.$error.minlength", (invalidity) -> if (invalidity and isDirty()) then show(error) else hide(error)

    do maxlengthError = ->
      error = encodedComponentModel "maxlengthError"

      element.after """
        <small id="#{error}" class="help-block invalid-text">#{scope.component.label} cannot be longer than #{scope.component.validations.maxLength} characters</small>
      """ if scope.component.validations?.maxLength?

      scope.$watch "inputCtrl.$error.maxlength", (invalidity) -> if (invalidity and isDirty()) then show(error) else hide(error)

    do patternError = ->
      error = encodedComponentModel "patternError"

      element.after """
        <small id="#{error}" class="help-block invalid-text">#{scope.component.label} #{scope.component.validations.regexp.error}</small>
      """ if scope.component.validations?.regexp

      scope.$watch "inputCtrl.$error.pattern", (invalidity) -> if (invalidity and isDirty()) then show(error) else hide(error)