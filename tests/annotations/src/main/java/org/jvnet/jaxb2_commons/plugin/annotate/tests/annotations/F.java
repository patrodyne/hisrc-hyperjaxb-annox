package org.jvnet.jaxb2_commons.plugin.annotate.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface F {
	String value();

}
