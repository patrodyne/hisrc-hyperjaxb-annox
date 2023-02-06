package org.jvnet.hyperjaxb_annox.tests.one;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTypeExampleTest
{
	private Logger logger = LoggerFactory.getLogger(JavaTypeExampleTest.class);
	public Logger getLogger() { return logger; }
	
	@Test
	public void rountripsBoolean()
		throws JAXBException
	{
		final JAXBContext context = JAXBContext.newInstance(JavaTypeExample.class);
		final JavaTypeExample value = new JavaTypeExample();
		value.setCustomBooleanProperty(true);
		
		final StringWriter sw = new StringWriter();
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(new JAXBElement<JavaTypeExample>(new QName("test"), JavaTypeExample.class, value), sw);
		getLogger().debug("JavaTypeExample :\n" + sw);
		
		assertTrue(sw.toString().contains(">true<"));
	}
}
