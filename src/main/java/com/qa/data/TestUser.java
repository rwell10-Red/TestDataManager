package com.qa.data;

/**
 * TestUser is a simple "model" class: each object holds the data for ONE row
 * of the test_users.xlsx spreadsheet.
 *
 * In QA automation this is called a POJO (Plain Old Java Object) or a "data
 * model". Instead of passing around loose strings, we group a user's fields
 * into one tidy object. That makes the rest of the code far easier to read:
 * `user.getRole()` is clearer than `row[4]`.
 */
public class TestUser {

    // --- Fields: the columns from the spreadsheet ---
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String role;           // e.g. "admin" or "customer"
    private final String expectedResult; // e.g. "pass" or "fail"

    /**
     * Constructor: builds one TestUser from its six column values.
     * A constructor is the special method that runs when you write `new TestUser(...)`.
     */
    public TestUser(String email, String password, String firstName,
                    String lastName, String role, String expectedResult) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.expectedResult = expectedResult;
    }

    // --- Getters: controlled read-access to the private fields (encapsulation) ---
    public String getEmail()          { return email; }
    public String getPassword()       { return password; }
    public String getFirstName()      { return firstName; }
    public String getLastName()       { return lastName; }
    public String getRole()           { return role; }
    public String getExpectedResult() { return expectedResult; }

    /** Convenience: the user's full name. */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
