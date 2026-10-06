Feature: Prescription request, approval and rejection
  As a patient
  I want to request a prescription and follow its status
  So that a doctor can review it and issue my digital prescription

  Background:
    Given a registered patient and a registered doctor
    And both have signed in and have an access token

  Scenario: Patient requests a new prescription
    Given a doctor and an active medication in the catalog
    When the patient requests a new prescription with that doctor and medication
    Then the request is created in pending status
    And the request belongs to the patient of the session

  Scenario: Request without medications
    When the patient requests a new prescription without any medication
    Then the system rejects the request with status 400

  Scenario: A doctor cannot request a prescription
    When the doctor tries to request a new prescription
    Then the system rejects the request with status 403

  Scenario: Doctor reviews a request
    Given a pending request assigned to the doctor
    When the doctor marks the request as in review
    Then the patient sees the request in review

  Scenario: Another doctor cannot attend the request
    Given a pending request assigned to the doctor
    When a different doctor tries to mark it as in review
    Then the system rejects the request with status 403

  Scenario: Doctor approves a request and issues the prescription
    Given a request in review
    When the doctor approves it with dose, frequency and treatment dates
    Then a digital prescription is issued
    And it has a unique 10-character verification code

  Scenario: Approve a request that was already attended
    Given a request that was already approved
    When the doctor approves it again
    Then the system rejects the request with status 400

  Scenario: Doctor rejects a request with a reason
    Given a pending request assigned to the doctor
    When the doctor rejects it with a reason
    Then the patient sees the request as rejected with that reason

  Scenario: Doctor rejects a request without a reason
    Given a pending request assigned to the doctor
    When the doctor rejects it with an empty reason
    Then the system rejects the request with status 400
