package org.example.po;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jaxb.runtime.api.JAXBRIContext;
import org.glassfish.jaxb.runtime.v2.model.annotation.RuntimeAnnotationReader;
import org.jvnet.basicjaxb_annox.xml.bind.AnnoxAnnotationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Example of unmarshalling a PurchaseOrder
 */
public class Main
{
	public static final String SAMPLE_PO_FILE = "src/test/samples/po.xml";
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	public static Logger getLogger() { return logger; }
	
    /**
     * Get a {@link JAXBContext} by constructing a default {@link AnnoxAnnotationReader}.
     * 
     * </p>
     * The default constructor binds a {@link DualAnnotatedElementFactory} which itself binds
     * to a {@link ResourcedAnnotatedElementFactory} and a {@link DirectAnnotatedElementFactory}.
     * </p>
     */
	private JAXBContext jaxbContext;
	public JAXBContext getJaxbContext() throws JAXBException
	{
		if ( jaxbContext == null )
		{
	        RuntimeAnnotationReader annotationReader = new AnnoxAnnotationReader();
	        Map<String, Object> properties = new HashMap<>();
	        properties.put(JAXBRIContext.ANNOTATION_READER, annotationReader);
	        Class<?>[] classes = { ObjectFactory.class };
			setJaxbContext(JAXBContext.newInstance(classes, properties));
		}
		return jaxbContext;
	}
	public void setJaxbContext(JAXBContext jaxbContext)
	{
		this.jaxbContext = jaxbContext;
	}

	private Unmarshaller unmarshaller = null;
	protected Unmarshaller getUnmarshaller() throws JAXBException
	{
		if ( unmarshaller == null )
			setUnmarshaller(getJaxbContext().createUnmarshaller());
		return unmarshaller;
	}
	protected void setUnmarshaller(Unmarshaller unmarshaller)
	{
		this.unmarshaller = unmarshaller;
	}

	// Represents the unmarshalled PurchaseOrder.
	private PurchaseOrder purchaseOrder = null;
	public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
	public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

	/**
	 * Command Line Invocation.
	 * @param args CLI argument(s)
	 */
	public static void main(String[] args) throws Exception
	{
		String path = args.length > 0 ? args[0] : SAMPLE_PO_FILE;
		Main main = new Main();
		main.execute(path);
	}
	
	private void execute(String path) throws JAXBException
	{
		File xmlFile = new File(path);
        setPurchaseOrder((PurchaseOrder) getUnmarshaller().unmarshal(xmlFile));
		println("PurchaseOrder: " + getPurchaseOrder().getOrderDate() + "\n\n" + getPurchaseOrder() + "\n");
	}

	/**
	 * Print a text string.
	 * @param text The text to be printed.
	 */
    public static void println(String text)
	{
		getLogger().info(text);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
