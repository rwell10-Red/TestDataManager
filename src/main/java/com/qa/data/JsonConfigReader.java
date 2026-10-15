package com.qa.data;

import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;

/**
 * JsonConfigReader reads JSON files using Jackson 3.x.
 *
 * Jackson 3 notes (vs Jackson 2):
 *  - Package is now "tools.jackson.*" (was "com.fasterxml.jackson.*").
 *  - JsonMapper is the recommended immutable mapper, built with JsonMapper.builder().build().
 *  - Jackson 3 throws UNCHECKED exceptions (JacksonException), so no "throws IOException"
 *    is required on these methods.
 *
 * Deserialization = turning JSON text into Java objects. Jackson matches each JSON
 * key to the Java field/setter of the same name.
 */
public class JsonConfigReader {

    // One shared, immutable mapper for the whole class.
    private final JsonMapper mapper = JsonMapper.builder().build();

    /**
     * Reads config.json into a single TestConfig object.
     * @param resourceName classpath name, e.g. "config.json"
     */
    public TestConfig readConfig(String resourceName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Could not find resource on classpath: " + resourceName);
            }
            // readValue(stream, Class) builds one object of that type from the JSON.
            return mapper.readValue(in, TestConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON config from " + resourceName, e);
        }
    }

    /**
     * Reads test_scenarios.json (a JSON array) into a List of TestScenario objects.
     * @param resourceName classpath name, e.g. "test_scenarios.json"
     */
    public List<TestScenario> readScenarios(String resourceName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Could not find resource on classpath: " + resourceName);
            }
            // For an array we read into TestScenario[], then wrap it in a List.
            TestScenario[] array = mapper.readValue(in, TestScenario[].class);
            return List.of(array);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON scenarios from " + resourceName, e);
        }
    }
}
