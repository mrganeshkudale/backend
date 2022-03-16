Feature: basic Owner API usage

  Scenario: Get existing owners
    When User requests to get existing Owners
    Then a list of Owners is returned
    And every Owner has a valid "id" value
    And every Owner has a valid "firstName" value

  Scenario: Create a new Owner
    Given an Owner request is created
    When a "create" request is submitted for the owner
    Then a new Owner is created in the system

  Scenario: Update an existing Owner
    Given an Owner is present in the system
    And the Owner's "address" value is changed to "New location"
    When an "update" request is submitted for the owner
    Then the Owner's "address" value should be "New location"

  Scenario: Delete an existing Owner
    Given an Owner is present in the system
    When a "delete" request is submitted for the owner
    Then the Owner should be deleted

    @Failing
    Scenario: Create a new Owner with blank mandatory field
      Given an Owner request is created
      And the Owner's "address" value is changed to ""
      When a "create" request is submitted for the owner
      Then a new Owner is created in the system
