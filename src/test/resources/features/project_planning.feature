Feature: Project planning

  Background:
    Given the system has a developer with initials "huba"

  Scenario: Create a project
    When a developer creates a project with name "WebShop"
    Then a project with name "WebShop" exists in the system

  Scenario: Create an activity and add it to a project
    Given a project with name "WebShop" exists
    When a developer creates an activity "Requirements" for the project
    Then the project "WebShop" has an activity named "Requirements"

  Scenario: Add a developer to an activity
    Given a project with name "WebShop" exists
    And the project has an activity named "Requirements"
    When developer "huba" is added to the activity "Requirements"
    Then activity "Requirements" has developer "huba" assigned

  Scenario: Assign a project leader
    Given a project with name "WebShop" exists
    When developer "huba" is assigned as project leader
    Then the project leader of "WebShop" is "huba"

  Scenario: Register time on an activity
    Given a project with name "WebShop" exists
    And the project has an activity named "Requirements"
    When developer "huba" registers 2.5 hours on activity "Requirements"
    Then activity "Requirements" has 2.5 registered hours

  Scenario: Generate project report
    Given a project with name "WebShop" exists
    And the project has an activity "Requirements" with a budget of 100.0 hours
    And developer "huba" has registered 30.0 hours on activity "Requirements"
    When a report is generated for the project
    Then the report shows 100.0 budgeted hours and 30.0 registered hours for "Requirements"

  Scenario: Register vacation as a fixed activity
    When developer "huba" registers vacation from week 20 2026 to week 22 2026
    Then developer "huba" is busy in week 21 2026
    And developer "huba" is not busy in week 19 2026

  Scenario: Available developers excludes developers assigned to activities
    Given a project with name "WebShop" exists
    And the project has an activity named "Requirements"
    And the activity has weeks 10 2026 to 12 2026
    And developer "huba" is added to the activity "Requirements"
    When available developers in week 11 2026 are requested
    Then developer "huba" is not in the available list

  Scenario: Available developers excludes developers on vacation
    When developer "huba" registers vacation from week 10 2026 to week 12 2026
    When available developers in week 11 2026 are requested
    Then developer "huba" is not in the available list
