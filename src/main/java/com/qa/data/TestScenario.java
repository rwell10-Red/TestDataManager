package com.qa.data;

/**
 * TestScenario is the model for one entry in test_scenarios.json.
 * The JSON file is an ARRAY of these, so Jackson will build a List<TestScenario>.
 *
 * Fields map to the JSON keys: id, name, category, type, priority.
 * - category = the FEATURE area (login, forgotPassword, session, ...)
 * - type     = the TEST TYPE (functional, negative, security, performance, ...)
 * - priority = business urgency (High / Medium / Low)
 */
public class TestScenario {

    private String id;
    private String name;
    private String category;   // feature area
    private String type;       // test type
    private String priority;   // High / Medium / Low

    /** No-arg constructor required by Jackson. */
    public TestScenario() {
    }

    // Getters
    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public String getType()     { return type; }
    public String getPriority() { return priority; }

    // Setters (used by Jackson)
    public void setId(String id)             { this.id = id; }
    public void setName(String name)         { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type)         { this.type = type; }
    public void setPriority(String priority) { this.priority = priority; }

    @Override
    public String toString() {
        return id + " | " + name + " [category=" + category
                + ", type=" + type + ", priority=" + priority + "]";
    }
}
