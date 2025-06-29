Feature: Analytics validation on UBS wealth-management contact form

  Scenario Outline: Validate analytics events triggered on page interaction
    Given I launch the UBS contact form page
    And Accept the Cookies
    And Enter FirstName as "<FName>"
    And Enter LastName
    And select sfCollection
    And Select phonePrefix
    And Enter Telephone
    And Enter email
    And Click on Submit Button
    And Select region
    Then I should see expected Adobe Analytics events "event193"
    Then I should see expected Adobe Analytics events "event13,event133"
    Then I should see expected Adobe Analytics events "event157"
    Then I should see expected Adobe Analytics events "event15"
    Then generateReport
    #And Click on Submit Button

    Examples:
      | FName |
      | Test  |
#      | Test2 |
#      | Test3 |