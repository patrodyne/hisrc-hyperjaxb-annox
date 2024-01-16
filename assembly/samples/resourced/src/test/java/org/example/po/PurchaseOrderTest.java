package org.example.po;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.glassfish.jaxb.runtime.api.JAXBRIContext;
import org.glassfish.jaxb.runtime.v2.model.annotation.RuntimeAnnotationReader;
import org.junit.jupiter.api.BeforeEach;
import org.jvnet.basicjaxb.testing.AbstractSamplesTest;
import org.jvnet.basicjaxb_annox.xml.bind.AnnoxAnnotationReader;
import org.patrodyne.jvnet.basicjaxb.validation.SchemaOutputDomResolver;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A JUnit test to check a sample XML file.
 */
public class PurchaseOrderTest extends AbstractSamplesTest
{
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() { return jaxbContext; }
	public void setJaxbContext(JAXBContext jaxbContext) { this.jaxbContext = jaxbContext; }

	private Unmarshaller unmarshaller;
	public Unmarshaller getUnmarshaller() { return unmarshaller; }
	public void setUnmarshaller(Unmarshaller unmarshaller) { this.unmarshaller = unmarshaller; }
	
	private Marshaller marshaller;
	public Marshaller getMarshaller() { return marshaller; }
	public void setMarshaller(Marshaller marshaller) { this.marshaller = marshaller; }

	/**
	 * Configure the JAXB context path in AbstractSamplesTest
	 * to include all classes from two packages.
	 */
	@Override
	protected String getContextPath()
	{
		return "org.example.po";
	}
	
	
    /**
     * Create a {@link JAXBContext} by constructing a default {@link AnnoxAnnotationReader}.
     * 
     * </p>
     * The default constructor binds a {@link DualAnnotatedElementFactory} which itself binds
     * to a {@link ResourcedAnnotatedElementFactory} and a {@link DirectAnnotatedElementFactory}.
     * </p>
     */
	@Override
	protected Map<String, ?> getContextProperties()
	{
		RuntimeAnnotationReader annotationReader = new AnnoxAnnotationReader();
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBRIContext.ANNOTATION_READER, annotationReader);
		return properties;
	}
	
	@BeforeEach
	public void beforeEach() throws JAXBException
	{
		setJaxbContext(createContext());
		setUnmarshaller(getJaxbContext().createUnmarshaller());
		setMarshaller(getJaxbContext().createMarshaller());
		getMarshaller().setProperty(JAXB_FORMATTED_OUTPUT, true);
		
		// Enable XML Schema Validation
		generateXmlSchemaValidatorFromDom();
	}
	
	/**
	 * Override the test method in AbstractSamplesTest to read
	 * this project's sample files and assert expectations.
	 */
	@Override
	protected void checkSample(File sample)	throws Exception
	{
		Object root = getUnmarshaller().unmarshal(sample);

		if ( root instanceof PurchaseOrder )
		{
			PurchaseOrder purchaseOrder = (PurchaseOrder) root;
			String purchaseOrderXml = marshal(marshaller, purchaseOrder);
			getLogger().debug("PurchaseOrder:\n\n" + purchaseOrderXml);
			
			assertNotNull(purchaseOrder.getOrderDate(), "PO OrderDate expected.");
			assertNotNull(purchaseOrder.getBillTo(), "PO BillTo expected.");
			assertNotNull(purchaseOrder.getShipTo(), "PO ShipTo expected.");
			assertNotNull(purchaseOrder.getComment(), "PO Comment expected.");

			getLogger().trace("PO OrderDate: " + purchaseOrder.getOrderDate());
			getLogger().trace("PO BillTo: " + purchaseOrder.getBillTo());
			getLogger().trace("PO ShipTo: " + purchaseOrder.getShipTo());
			getLogger().trace("PO Comment: " + purchaseOrder.getComment());
			
			assertNotNull(purchaseOrder.getItems(), "PO Items expected.");
			assertFalse(purchaseOrder.getItems().getItem().isEmpty(), "PO Items not empty expected.");
			for (  Item item : purchaseOrder.getItems().getItem() )
			{
				assertNotNull(item.getPartNum(), "Item must have a part number");
				getLogger().trace("PO Item: " + item);
			}
		}
		else
			fail("Object is not instance of PurchaseOrder: " + root);
	}

	// Marshall a JAXB object into a String for logging, etc.
	private String marshal(Marshaller marshaller, Object jaxbObject)
		throws JAXBException, IOException
	{
		String xml;
		try ( StringWriter sw = new StringWriter() )
		{
			marshaller.marshal(jaxbObject, sw);
			xml = sw.toString();
		}
		return xml;
	}

	private void generateXmlSchemaValidatorFromDom()
	{
		try
		{
			if ( (getMarshaller() != null) && (getUnmarshaller() != null) )
			{
				// Generate a Schema Validator from given the JAXB context.
				SchemaOutputDomResolver sodr = new SchemaOutputDomResolver();
				getJaxbContext().generateSchema(sodr);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
				Schema schemaValidator = schemaFactory.newSchema(sodr.getDomSource());
				
				// Configure Marshaller / unmarshaller to use validator.
				getMarshaller().setSchema(schemaValidator);
				getUnmarshaller().setSchema(schemaValidator);
				
				println("Schema Validation of XML is ON.");
			}
			else
				errorln("Please create marshaller and unmarshaller!");
		}
		catch ( IOException | SAXException ex )
		{
			errorln("generateXmlSchemaValidatorFromDom", ex);
		}
	}

	private void errorln(String msg)
	{
		getLogger().error(msg);
	}
	
	private void errorln(String msg, Exception ex)
	{
		getLogger().error(msg, ex);
		
	}
	
	private void println(String msg)
	{
		getLogger().info(msg);
	}
}
