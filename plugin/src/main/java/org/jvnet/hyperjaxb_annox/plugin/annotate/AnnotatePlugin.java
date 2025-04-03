package org.jvnet.hyperjaxb_annox.plugin.annotate;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jvnet.basicjaxb.util.CustomizationUtils.getCustomizations;
import static org.jvnet.basicjaxb.util.LocatorUtils.toLocation;
import static org.jvnet.basicjaxb_annox.Constants.NAMESPACE_URI;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.CLASS;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.ELEMENT;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.ENUM;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.ENUM_CONSTANT;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.getAnnotationTarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.jvnet.basicjaxb.plugin.AbstractParameterizablePlugin;
import org.jvnet.basicjaxb.plugin.AbstractPlugin;
import org.jvnet.basicjaxb_annox.model.XAnnotation;
import org.jvnet.basicjaxb_annox.parser.XAnnotationParser;
import org.jvnet.basicjaxb_annox.parser.exception.AnnotationElementParseException;
import org.jvnet.basicjaxb_annox.parser.exception.AnnotationExpressionParseException;
import org.jvnet.basicjaxb_annox.parser.exception.AnnotationStringParseException;
import org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget;
import org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationStringValue;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.ElementOutline;
import com.sun.tools.xjc.outline.EnumConstantOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * An XJC plugin to add an annotation class to a given target. The targets are
 * enumerated by {@link AnnotationTarget}.
 */
public class AnnotatePlugin extends AbstractParameterizablePlugin
{
	/** Name of Option to enable this plugin. */
	private static final String OPTION_NAME = "Xannotate";
	
	/** Description of Option to enable this plugin. */
	private static final String OPTION_DESC = "add arbitrary annotations to generated sources";
	
	// Binding names
	public static final QName ANNOTATE_QNAME = new QName(NAMESPACE_URI, "annotate");
	public static final QName ANNOTATE_PROPERTY_QNAME = new QName(NAMESPACE_URI, "annotateProperty");
	public static final QName ANNOTATE_PROPERTY_GETTER_QNAME = new QName(NAMESPACE_URI, "annotatePropertyGetter");
	public static final QName ANNOTATE_PROPERTY_SETTER_QNAME = new QName(NAMESPACE_URI, "annotatePropertySetter");
	public static final QName ANNOTATE_PROPERTY_FIELD_QNAME = new QName(NAMESPACE_URI, "annotatePropertyField");
	public static final QName ANNOTATE_PROPERTY_SETTER_PARAMETER_QNAME = new QName(NAMESPACE_URI, "annotatePropertySetterParameter");
	public static final QName ANNOTATE_PACKAGE_QNAME = new QName(NAMESPACE_URI, "annotatePackage");
	public static final QName ANNOTATE_OBJECT_FACTORY_QNAME = new QName(NAMESPACE_URI, "annotateObjectFactory");
	public static final QName ANNOTATE_CLASS_QNAME = new QName(NAMESPACE_URI, "annotateClass");
	public static final QName ANNOTATE_ELEMENT_QNAME = new QName(NAMESPACE_URI, "annotateElement");
	public static final QName ANNOTATE_ENUM_QNAME = new QName(NAMESPACE_URI, "annotateEnum");
	public static final QName ANNOTATE_ENUM_CONSTANT_QNAME = new QName(NAMESPACE_URI, "annotateEnumConstant");
	public static final QName ANNOTATE_ENUM_VALUE_METHOD_QNAME = new QName(NAMESPACE_URI, "annotateEnumValueMethod");
	public static final QName ANNOTATE_ENUM_FROM_VALUE_METHOD_QNAME = new QName(NAMESPACE_URI, "annotateEnumFromValueMethod");
	
	public static final Set<QName> CUSTOMIZATION_ELEMENT_QNAMES = unmodifiableSet(new HashSet<QName>(
		asList(
			ANNOTATE_QNAME,
			ANNOTATE_PACKAGE_QNAME,
			ANNOTATE_OBJECT_FACTORY_QNAME,
			ANNOTATE_CLASS_QNAME,
			ANNOTATE_ELEMENT_QNAME,
			ANNOTATE_PROPERTY_QNAME,
			ANNOTATE_PROPERTY_FIELD_QNAME,
			ANNOTATE_PROPERTY_GETTER_QNAME,
			ANNOTATE_PROPERTY_SETTER_QNAME,
			ANNOTATE_PROPERTY_SETTER_PARAMETER_QNAME,
			ANNOTATE_ENUM_QNAME,
			ANNOTATE_ENUM_CONSTANT_QNAME,
			ANNOTATE_ENUM_VALUE_METHOD_QNAME,
			ANNOTATE_ENUM_FROM_VALUE_METHOD_QNAME)));

	@Override
	public String getOptionName()
	{
		return OPTION_NAME;
	}

	@Override
	public String getUsage()
	{
		return format(USAGE_FORMAT, OPTION_NAME, OPTION_DESC);
	}

	@Override
	public Collection<QName> getCustomizationElementNames()
	{
		return CUSTOMIZATION_ELEMENT_QNAMES;
	}
	
	private boolean isCustomizationElementName(final QName name)
	{
		return (name != null) && NAMESPACE_URI.equals(name.getNamespaceURI())
			&& !RemoveAnnotationPlugin.CUSTOMIZATION_ELEMENT_QNAMES.contains(name);
	}
	
	private String defaultFieldTarget = "getter";
	public String getDefaultFieldTarget()
	{
		return defaultFieldTarget;
	}
	public void setDefaultFieldTarget(String defaultFieldTarget)
	{
		if
		(
			"getter".equals(defaultFieldTarget) ||
			"setter".equals(defaultFieldTarget) ||
			"setter-parameter".equals(defaultFieldTarget) ||
			"field".equals(defaultFieldTarget)
		)
		{
			this.defaultFieldTarget = defaultFieldTarget;
		}
		else
			throw new IllegalArgumentException("Invalid default field target.");
	}

	private XAnnotationParser annotationParser = XAnnotationParser.INSTANCE;
	public XAnnotationParser getAnnotationParser()
	{
		return annotationParser;
	}
	public void setAnnotationParser(XAnnotationParser annotationParser)
	{
		this.annotationParser = annotationParser;
	}

	private Annotator annotator = new Annotator();
	public Annotator getAnnotator()
	{
		return annotator;
	}
	public void setAnnotator(Annotator annotator)
	{
		this.annotator = annotator;
	}

	// Plugin Processing
	
	@Override
	protected void beforeRun(Outline outline) throws Exception
	{
		if ( isInfoEnabled() )
		{
			StringBuilder sb = new StringBuilder();
			sb.append(LOGGING_START);
			sb.append("\nParameters");
			sb.append("\n  DefaultFieldTarget.: " + getDefaultFieldTarget());
			sb.append("\n  AnnotationParser...: " + getAnnotationParser());
			sb.append("\n  Annotator..........: " + getAnnotator());
			sb.append("\n  Verbose............: " + isVerbose());
			sb.append("\n  Debug..............: " + isDebug());
			info(sb.toString());
		}
	}
	
	@Override
	protected void afterRun(Outline outline) throws Exception
	{
		if ( isInfoEnabled() )
		{
			StringBuilder sb = new StringBuilder();
			sb.append(LOGGING_FINISH);
			sb.append("\nResults");
			sb.append("\n  HadError.: " + hadError(outline.getErrorReceiver()));
			info(sb.toString());
		}
	}
	
	/**
	 * <p>
	 * Run the plugin with and XJC {@link Outline}.
	 * </p>
	 * 
	 * <p>
	 * Run an XJC plugin to add or modify the XJC {@link Outline}. An {@link Outline}
	 * captures which code is generated for which model component. A {@link Model} is
	 * a schema language neutral representation of the result of a schema parsing. XJC
	 * uses this model to turn this into a series of Java source code.
	 * </p>
	 * 
     * <p>
     * <b>Note:</b> This method is invoked only when a plugin is activated.
     * </p>
	 *
     * @param outline
     *      This object allows access to various generated code.
     * 
     * @return
     *      If the add-on executes successfully, return true.
     *      If it detects some errors but those are reported and
     *      recovered gracefully, return false.
     *
     * @throws Exception
     *      This 'run' method is a call-back method from {@link AbstractPlugin}
     *      and that method is responsible for handling all exceptions. It reports
     *      any exception to {@link ErrorHandler} and converts the exception to
     *      a {@link SAXException} for processing by {@link com.sun.tools.xjc.Plugin}.
	 */
	@Override
	public boolean run(Outline outline) throws Exception
	{
		for (final CElementInfo elementInfo : outline.getModel().getAllElements())
		{
			final ElementOutline elementOutline = outline.getElement(elementInfo);
			if (elementOutline != null)
				processElementOutline(elementOutline);
		}
		
		for (final ClassOutline classOutline : outline.getClasses())
			processClassOutline(classOutline);
		
		for (final EnumOutline enumOutline : outline.getEnums())
			processEnumOutline(enumOutline);
		
		return !hadError(outline.getErrorReceiver());
	}

	protected void processElementOutline(ElementOutline elementOutline) throws SAXParseException
	{
		final CCustomizations customizations = getCustomizations(elementOutline);
		debug("{}, processElementOutline; Customizations: {}", toLocation(elementOutline),
			customizations != null ? customizations.size() : 0);
		annotateElementOutline(elementOutline, customizations);
	}

	protected void annotateElementOutline(final ElementOutline elementOutline, final CCustomizations customizations)
		throws SAXParseException
	{
		final JDefinedClass theElementClass = elementOutline.implClass;
		final JCodeModel theElementOwner = theElementClass.owner();

		for (final CPluginCustomization customization : customizations)
		{
			final Element element = customization.element;
			final QName name = new QName(element.getNamespaceURI(), element.getLocalName());
			if (isCustomizationElementName(name))
			{
				customization.markAsAcknowledged();
				final AnnotationTarget annotationTarget = getAnnotationTarget(element, ELEMENT);
				try
				{
					final JAnnotatable annotatable =
						annotationTarget.getAnnotatable(elementOutline.parent(), elementOutline);
					if ( annotate(theElementOwner, customization, element, annotatable) )
					{
						debug("{}, annotateElementOutline; Class={}",
							toLocation(customization, elementOutline), theElementClass.name());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	protected void processClassOutline(ClassOutline classOutline) throws SAXParseException
	{
		final CCustomizations customizations = getCustomizations(classOutline);
		debug("{}, processClassOutline; Customizations: {}", toLocation(classOutline),
			customizations != null ? customizations.size() : 0);
		annotateClassOutline(classOutline, customizations);

		for (final FieldOutline fieldOutline : classOutline.getDeclaredFields())
			processFieldOutline(classOutline, fieldOutline);
		
		// Process XmlTransient fields to be JAXB exclusive.
		processXmlTransient((ClassOutlineImpl) classOutline);
	}

	protected void annotateClassOutline(final ClassOutline classOutline, final CCustomizations customizations)
		throws SAXParseException
	{
		final JDefinedClass theClassRef = classOutline.ref;
		final JCodeModel theClassRefOwner = theClassRef.owner();

		for (final CPluginCustomization customization : customizations)
		{
			final Element element = customization.element;
			final QName name = new QName(element.getNamespaceURI(), element.getLocalName());
			if (isCustomizationElementName(name))
			{
				customization.markAsAcknowledged();
				final AnnotationTarget annotationTarget = getAnnotationTarget(element, CLASS);
				try
				{
					final JAnnotatable annotatable =
						annotationTarget.getAnnotatable(classOutline.parent(), classOutline);
					if ( annotate(theClassRefOwner, customization, element, annotatable) )
					{
						debug("{}, annotateClassOutline; Class={}",
							toLocation(customization, classOutline), theClassRef.name());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	// Process XmlTransient fields to be JAXB exclusive and to be omitted
	// from the XmlType property order.
	protected void processXmlTransient(ClassOutlineImpl coi)
	{
		// Create a map of the XmlTransient field(s) to be removed from the propOrder.
		Set<String> xmlTransientFieldSet = new HashSet<>();
		for ( Entry<String, JFieldVar> fieldEntry : coi.implClass.fields().entrySet() )
		{
			boolean isXmlTransientField = false;
			Set<JAnnotationUse> excludeAnnotations = new HashSet<>();
			JFieldVar fieldValue = fieldEntry.getValue();
			for ( JAnnotationUse fieldAnnotation : fieldValue.annotations() )
			{
				JClass fieldAnnotationClass = fieldAnnotation.getAnnotationClass();
				String fieldAnnotationClassFullName = fieldAnnotationClass.fullName();
				if ( XmlTransient.class.getName().equals(fieldAnnotationClassFullName) )
				{
					isXmlTransientField = true;
					xmlTransientFieldSet.add(fieldEntry.getKey());
				}
				else
				{
					// The field annotation is not XmlTransient. If it is a
					// JAXB annotation then add it to the list of annotations
					// that might be excluded.
					//
					// This set will be excluded ONLY when isXmlTransientField
					// is true; otherwise, this set will be discarded.
					if ( fieldAnnotationClassFullName.startsWith(XmlElement.class.getPackageName()))
						excludeAnnotations.add(fieldAnnotation);
				}
			}

			// @XmlTransient is mutually exclusive with all other
			// Jakarta XML Binding defined annotations.
			if ( isXmlTransientField )
			{
				for ( JAnnotationUse excludeAnnotation : excludeAnnotations )
					fieldValue.removeAnnotation(excludeAnnotation);
			}
		}

		// Prepare to remove XmlTransient field(s) from the XmlType's property order.
		boolean replaceXmlTypeAnnUse = false;
		List<String> auXmlTypePropOrderListNew = new ArrayList<>();
		JAnnotationUse auXmlTypeOld = null;
		for ( JAnnotationUse au : coi.implClass.annotations() )
		{
			// Is this annotation for XmlType?
			if ( XmlType.class.getName().equals(au.getAnnotationClass().fullName()) )
			{
				auXmlTypeOld = au;
				// Is there a property order member as an array.
				JAnnotationValue auPropOrderValueOld = auXmlTypeOld.getAnnotationMembers().get("propOrder");
				if ( auPropOrderValueOld instanceof JAnnotationArrayMember )
				{
					JAnnotationArrayMember auPropOrderMemberOld = (JAnnotationArrayMember) auPropOrderValueOld;
					for ( JAnnotationValue auPropOrderMemberValueOld : auPropOrderMemberOld.annotations2() )
					{
						if ( auPropOrderMemberValueOld instanceof JAnnotationStringValue )
						{
							String auPropOrderMemberStringOld = auPropOrderMemberValueOld.toString();
							// Exclude transient fields from the property order.
							if ( xmlTransientFieldSet.contains(auPropOrderMemberStringOld) )
								replaceXmlTypeAnnUse = true;
							else
								auXmlTypePropOrderListNew.add(auPropOrderMemberStringOld);
						}
					}
				}
			}
		}
		
		// Is there a XMlType annotation that must be replaced with
		// an XmlTransient exclusive property order?
		if ( replaceXmlTypeAnnUse )
		{
			coi.implClass.removeAnnotation(auXmlTypeOld);
			
			JAnnotationUse auXmlTypeNew =
				coi.implClass.annotate((auXmlTypeOld.getAnnotationClass()));

			JAnnotationValue namespace = auXmlTypeOld.getAnnotationMembers().get("namespace");
			if ( namespace != null )
				auXmlTypeNew.param("namespace", namespace);

			JAnnotationValue name = auXmlTypeOld.getAnnotationMembers().get("name");
			if ( name != null )
				auXmlTypeNew.param("name", name);
			
			JAnnotationArrayMember auPropOrderMemberNew = auXmlTypeNew.paramArray("propOrder");
			for ( String propOrderItem : auXmlTypePropOrderListNew )
				auPropOrderMemberNew.param(propOrderItem);
			
			JAnnotationValue factoryClass = auXmlTypeOld.getAnnotationMembers().get("factoryClass");
			if ( factoryClass != null )
				auXmlTypeNew.param("factoryClass", factoryClass);
			
			JAnnotationValue factoryMethod = auXmlTypeOld.getAnnotationMembers().get("factoryMethod");
			if ( factoryMethod != null )
				auXmlTypeNew.param("factoryMethod", factoryMethod);
		}
	}
	
	protected void processFieldOutline(ClassOutline classOutline, FieldOutline fieldOutline)
		throws SAXParseException
	{
		final CCustomizations customizations = getCustomizations(fieldOutline);
		debug("{}, processFieldOutline; Customizations: {}", toLocation(fieldOutline),
			customizations != null ? customizations.size() : 0);
		annotateFieldOutline(fieldOutline, customizations);
	}

	protected void annotateFieldOutline(final FieldOutline fieldOutline, final CCustomizations customizations)
		throws SAXParseException
	{
		final CPropertyInfo fieldInfo = fieldOutline.getPropertyInfo();
		final JDefinedClass fieldParentRef = fieldOutline.parent().ref;
		final JCodeModel fieldParentRefOwner = fieldParentRef.owner();

		for (final CPluginCustomization customization : customizations)
		{
			final Element element = customization.element;
			final QName name = new QName(element.getNamespaceURI(), element.getLocalName());
			if (isCustomizationElementName(name))
			{
				customization.markAsAcknowledged();
				final AnnotationTarget annotationTarget =
					getAnnotationTarget(element, getAnnotationTarget(getDefaultFieldTarget()));
				try
				{
					final JAnnotatable annotatable =
						annotationTarget.getAnnotatable(fieldOutline.parent().parent(), fieldOutline);
					if ( annotate(fieldParentRefOwner, customization, element, annotatable) )
					{
						trace("{}, annotateFieldOutline; Class={}, Field={}",
							toLocation(customization, fieldOutline), fieldParentRef.name(), fieldInfo.getName(false));
					}
				}
				catch (IllegalArgumentException iaex)
				{
					error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	protected void processEnumOutline(EnumOutline enumOutline) throws SAXParseException
	{
		final CCustomizations customizations = getCustomizations(enumOutline);
		debug("{}, processEnumOutline; Customizations: {}", toLocation(enumOutline),
			customizations != null ? customizations.size() : 0);
		annotateEnumOutline(enumOutline, customizations);
		for (final EnumConstantOutline enumConstantOutline : enumOutline.constants)
			processEnumConstantOutline(enumOutline, enumConstantOutline);
	}

	protected void annotateEnumOutline(final EnumOutline enumOutline, final CCustomizations customizations)
		throws SAXParseException
	{
		final JDefinedClass theEnumClass = enumOutline.clazz;
		final JCodeModel theEnumClassOwner = theEnumClass.owner();

		for (final CPluginCustomization customization : customizations)
		{
			final Element element = customization.element;
			final QName name = new QName(element.getNamespaceURI(), element.getLocalName());
			if (isCustomizationElementName(name))
			{
				customization.markAsAcknowledged();
				final AnnotationTarget annotationTarget = getAnnotationTarget(element, ENUM);
				try
				{
					final JAnnotatable annotatable =
						annotationTarget.getAnnotatable(enumOutline.parent(), enumOutline);
					if ( annotate(theEnumClassOwner, customization, element, annotatable) )
					{
						debug("{}, annotateEnumOutline; Enum={}",
							toLocation(customization, enumOutline), theEnumClass.name());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	protected void processEnumConstantOutline(EnumOutline enumOutline, EnumConstantOutline enumConstantOutline)
		throws SAXParseException
	{
		final CCustomizations customizations = getCustomizations(enumConstantOutline);
		debug("{}, processEnumConstantOutline; Customizations: {}", toLocation(enumConstantOutline),
			customizations != null ? customizations.size() : 0);
		annotateEnumConstantOutline(enumOutline, enumConstantOutline, customizations);
	}

	protected void annotateEnumConstantOutline(final EnumOutline enumOutline,
		final EnumConstantOutline enumConstantOutline, final CCustomizations customizations) throws SAXParseException
	{
		final JDefinedClass enumClass = enumOutline.clazz;
		final Outline enumOutlineParent = enumOutline.parent();
		final JCodeModel enumOutlineParentOwner = enumOutlineParent.getCodeModel();

		for (final CPluginCustomization customization : customizations)
		{
			final Element element = customization.element;
			final QName name = new QName(element.getNamespaceURI(), element.getLocalName());
			if (isCustomizationElementName(name))
			{
				customization.markAsAcknowledged();
				final AnnotationTarget annotationTarget = getAnnotationTarget(element, ENUM_CONSTANT);
				try
				{
					final JAnnotatable annotatable =
						annotationTarget.getAnnotatable(enumOutlineParent, enumConstantOutline);
					if ( annotate(enumOutlineParentOwner, customization, element, annotatable) )
					{
						trace("{}, annotateEnumConstantOutline; Class={}, EnumConstant={}",
							toLocation(customization, enumOutline), enumClass.name(), enumConstantOutline.constRef.getName());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	private boolean annotate(final JCodeModel codeModel, final CPluginCustomization customization,
		final Element element, final JAnnotatable annotatable) throws SAXParseException
	{
		boolean annotated = false;
		try
		{
			final NodeList elements = element.getChildNodes();
			for (int index = 0; index < elements.getLength(); index++)
			{
				final Node node = elements.item(index);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					final Element child = (Element) node;
					final XAnnotation<?> annotation = getAnnotationParser().parse(child);
					getAnnotator().annotate(codeModel, annotatable, annotation);
					annotated = true;
				}
				else if (node.getNodeType() == Node.TEXT_NODE)
				{
					final String nodeValue = node.getNodeValue();
					if ( isNotBlank(nodeValue) )
					{
						final XAnnotation<?> annotation = getAnnotationParser().parse(nodeValue);
						getAnnotator().annotate(codeModel, annotatable, annotation);
						annotated = true;
					}
				}
			}
		}
		catch (AnnotationElementParseException | AnnotationStringParseException | AnnotationExpressionParseException ex)
		{
			throw new SAXParseException("Cannot annotate " + annotatable, customization.locator, ex);
		}
		return annotated;
	}
}
