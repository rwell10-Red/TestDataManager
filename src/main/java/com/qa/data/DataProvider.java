package com.qa.data;

import java.util.List;

/**
 * DataProvider is the CONTRACT for any class that supplies test data.
 *
 * An interface lists WHAT a class must be able to do, not HOW it does it.
 * Think of it as a job description: "a data provider must be able to read a
 * list of test users and tell us its source."  Whether that data comes from
 * Excel, a database, or a CSV file is each implementation's own business.
 *
 * Why QA engineers care: test code can depend on this interface instead of a
 * specific file format. Swap Excel for a database later and the tests that use
 * a DataProvider never change. This is "programming to an interface".
 *
 * Note (from the Oracle Java Tutorial): interface methods are implicitly
 * public, and an abstract method ends with a semicolon and has no body.
 */
public interface DataProvider {

    /**
     * Read all test users from the underlying source.
     * @return a List of TestUser objects (never null; empty if no data)
     */
    List<TestUser> readData();

    /**
     * @return a human-readable description of where the data came from,
     *         e.g. the file name. Useful for logging in test reports.
     */
    String getSourceName();
}
