Feature: Prescription renewal
  As a patient
  I want to renew a prescription I already received
  So that I do not have to describe my treatment again

  Background:
    Given a patient with an approved prescription issued by a doctor
    And the patient has signed in

  Scenario: Patient renews their own prescription
    When the patient requests the renewal of the prescription
    Then a renewal request is created for the doctor who issued it
    And it keeps the same medications

  Scenario: Renewal without indicating the prescription
    When the patient requests a renewal without a prescription
    Then the system rejects the request with status 400

  Scenario: Renewal of a prescription that does not exist
    When the patient requests the renewal of a non-existent prescription
    Then the system rejects the request with status 404

  Scenario: Renewal of another patient's prescription
    Given a second patient
    When the second patient requests the renewal of that prescription
    Then the system rejects the request with status 403

  Scenario: Second renewal while one is in progress
    Given a renewal already in progress for the prescription
    When the patient requests another renewal of the same prescription
    Then the system rejects the request with status 400

  Scenario: Doctor approves a renewal
    Given a renewal request assigned to the doctor
    When the doctor approves it
    Then a new prescription is issued
    And the request keeps a reference to the prescription that was renewed
