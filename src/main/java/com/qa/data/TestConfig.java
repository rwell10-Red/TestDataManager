package com.qa.data;

/**
 * TestConfig is the model for config.json. One object = the whole config file.
 *
 * The field NAMES must match the JSON keys (baseUrl, browser, timeout, retries)
 * so Jackson can map each JSON value onto the matching field automatically.
 *
 * Jackson needs a no-argument constructor to create the empty object before it
 * fills in the fields, so we provide one explicitly.
 */
public class TestConfig {

    private String baseUrl;
    private String browser;
    private int timeout;   // seconds
    private int retries;

    /** No-arg constructor required by Jackson for deserialization. */
    public TestConfig() {
    }

    // Getters (read access)
    public String getBaseUrl() { return baseUrl; }
    public String getBrowser() { return browser; }
    public int getTimeout()    { return timeout; }
    public int getRetries()    { return retries; }

    // Setters (Jackson uses these to populate the object from JSON)
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setBrowser(String browser) { this.browser = browser; }
    public void setTimeout(int timeout)    { this.timeout = timeout; }
    public void setRetries(int retries)    { this.retries = retries; }

    @Override
    public String toString() {
        return "baseUrl=" + baseUrl + ", browser=" + browser
                + ", timeout=" + timeout + "s, retries=" + retries;
    }
}
