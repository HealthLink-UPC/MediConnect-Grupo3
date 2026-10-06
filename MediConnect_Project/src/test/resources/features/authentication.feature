Feature: Registration and authentication
  As a patient or a doctor
  I want to register and sign in
  So that I can access MediConnect with the permissions of my role

  Scenario: Register a patient with valid data
    Given a visitor with a valid email, password, name, DNI, birth date and phone
    When the visitor registers as a patient
    Then the account is created
    And the patient can sign in

  Scenario: Register a patient with incomplete data
    Given a visitor who only provides an email and a password
    When the visitor registers as a patient
    Then the system rejects the request with status 400
    And no account is created

  Scenario: Register a doctor without professional data
    Given a visitor without specialty, CMP or care center
    When the visitor registers as a doctor
    Then the system rejects the request with status 400

  Scenario: Register with an email that already exists
    Given a patient already registered with an email
    When another visitor registers with the same email
    Then the system rejects the request with status 400

  Scenario: Sign in with valid credentials
    Given a registered patient
    When the patient signs in with the correct email and password
    Then the system returns an access token, the role and the patient identifier

  Scenario: Sign in with a wrong password
    Given a registered patient
    When the patient signs in with a wrong password
    Then the system rejects the request with status 401

  Scenario Outline: Access a protected service without a valid token
    Given a user without a token or with an invalid token
    When the user requests "<service>"
    Then the system rejects the request with status 401

    Examples:
      | service            |
      | GET /medicos/listar |
      | GET /catalogo/alergias |
