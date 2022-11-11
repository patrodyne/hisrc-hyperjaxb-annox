package org.jvnet.hyperjaxb_annox.plugin.annotate.tests.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface F {
	String value();

}
