package com.paramount.test.ff.uitests.helpers.leftfilters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the manual test id and human-readable scenario shown in the left-filter execution email.
 * Add this annotation on each left-filter TestNG class when {@code LeftFilterEmailReport=true} in the suite XML.
 *
 * <p>Generic scenario pattern (reuse for other filters/tabs):
 * {@code "{Filter name} left filter — Selected option count matches {tab} table record count and {column} column values"}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LeftFilterEmailScenario {

    /** Manual / TestRail style id, e.g. {@code LF_O_TC601}. */
    String manualId();

    /** Scenario description for the email report table. */
    String scenario();
}
