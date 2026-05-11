package com.planner.steps;
// Nat

// This class is the entry point for all Cucumber BDD tests.
// It tells JUnit to run the Cucumber engine and look for feature files
// in the "features" folder on the classpath.
//
// The actual test scenarios are written in plain English in:
//   src/test/resources/features/project_planning.feature
//
// Each scenario in the feature file corresponds to a use case from Report 1.
// The step definitions that connect the scenarios to Java code are in StepDefinitions.java.
//
// "pretty" plugin makes the test output readable in the terminal — it prints
// each Given/When/Then step with pass/fail status.

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite                                          // marks this as a JUnit test suite
@IncludeEngines("cucumber")                     // use the Cucumber test engine
@SelectClasspathResource("features")           // look for .feature files in the features folder
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty") // readable terminal output
public class CucumberRunnerTest {
}
