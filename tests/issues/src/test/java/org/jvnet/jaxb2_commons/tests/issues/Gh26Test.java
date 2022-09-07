package org.jvnet.jaxb2_commons.tests.issues;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;

import jakarta.xml.bind.annotation.XmlElement;

import org.junit.jupiter.api.Test;

public class Gh26Test {

	@Test
	public void fiedlYIsNotAnnotated() throws Exception {

		Field yField = Gh26Type.class.getDeclaredField("y");
		assertThat(yField.getAnnotation(XmlElement.class), is(nullValue()));
	}
}
