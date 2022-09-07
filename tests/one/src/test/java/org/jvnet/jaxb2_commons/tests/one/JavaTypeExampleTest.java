package org.jvnet.jaxb2_commons.tests.one;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

public class JavaTypeExampleTest {

	@Test
	public void rountripsBoolean() throws JAXBException {
		final JAXBContext context = JAXBContext
				.newInstance(JavaTypeExample.class);

		final JavaTypeExample value = new JavaTypeExample();
		value.setCustomBooleanProperty(true);
		final StringWriter sw = new StringWriter();
		context.createMarshaller().marshal(
				new JAXBElement<JavaTypeExample>(new QName("test"),
						JavaTypeExample.class, value), sw);
		assertTrue(sw.toString().contains(">true<"));
//		System.out.println(sw.toString());
	}
}
