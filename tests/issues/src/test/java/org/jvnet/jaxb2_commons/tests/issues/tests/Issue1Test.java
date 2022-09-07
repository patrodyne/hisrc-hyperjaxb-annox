package org.jvnet.jaxb2_commons.tests.issues.tests;

import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.jvnet.jaxb2_commons.tests.issues.ObjectFactory;

public class Issue1Test {

	@Test
	public void testPackageInfoIsAnnotated() {
		final XmlSchema xmlSchema = ObjectFactory.class.getPackage()
				.getAnnotation(XmlSchema.class);
		assertEquals(XmlNsForm.QUALIFIED, xmlSchema.elementFormDefault());
	}

}
