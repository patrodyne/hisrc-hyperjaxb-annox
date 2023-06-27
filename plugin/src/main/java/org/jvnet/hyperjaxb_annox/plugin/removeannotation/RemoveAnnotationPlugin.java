package org.jvnet.hyperjaxb_annox.plugin.removeannotation;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.jvnet.basicjaxb.util.LocatorUtils.toLocation;
import static org.jvnet.basicjaxb_annox.Constants.NAMESPACE_URI;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.CLASS;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.ELEMENT;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.ENUM;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.ENUM_CONSTANT;
import static org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget.getAnnotationTarget;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jvnet.basicjaxb.plugin.AbstractParameterizablePlugin;
import org.jvnet.basicjaxb.plugin.AbstractPlugin;
import org.jvnet.basicjaxb.util.CustomizationUtils;
import org.jvnet.hyperjaxb_annox.plugin.AnnotationTarget;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
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

/**
 * An XJC plugin to remove an annotation class for a given target. The targets are
 * enumerated by {@link AnnotationTarget}.
 */
public class RemoveAnnotationPlugin extends AbstractParameterizablePlugin
{
	/** Name of Option to enable this plugin. */
	private static final String OPTION_NAME = "XremoveAnnotation";
	
	/** Description of Option to enable this plugin. */
	private static final String OPTION_DESC = "remove specific, targeted annotation(s)";

	// Binding names
	public static final String CLASS_ATTRIBUTE_NAME = "class";
	public static final QName REMOVE_ANNOTATION_QNAME = new QName(NAMESPACE_URI, "removeAnnotation");
	public static final QName REMOVE_ANNOTATION_FROM_PROPERTY_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromProperty");
	public static final QName REMOVE_ANNOTATION_FROM_PROPERTY_GETTER_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromPropertyGetter");
	public static final QName REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromPropertySetter");
	public static final QName REMOVE_ANNOTATION_FROM_PROPERTY_FIELD_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromPropertyField");
	public static final QName REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_PARAMETER_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromPropertySetterParameter");
	public static final QName REMOVE_ANNOTATION_FROM_PACKAGE_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromPackage");
	public static final QName REMOVE_ANNOTATION_FROM_CLASS_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromClass");
	public static final QName REMOVE_ANNOTATION_FROM_ELEMENT_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromElement");
	public static final QName REMOVE_ANNOTATION_FROM_ENUM_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromEnum");
	public static final QName REMOVE_ANNOTATION_FROM_ENUM_CONSTANT_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromEnumConstant");
	public static final QName REMOVE_ANNOTATION_FROM_ENUM_VALUE_METHOD_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromEnumValueMethod");
	public static final QName REMOVE_ANNOTATION_FROM_ENUM_FROM_VALUE_METHOD_QNAME = new QName(NAMESPACE_URI, "removeAnnotationFromEnumFromValueMethod");

	public static final Set<QName> CUSTOMIZATION_ELEMENT_QNAMES = unmodifiableSet(new HashSet<QName>(
		asList(
			REMOVE_ANNOTATION_QNAME,
			REMOVE_ANNOTATION_FROM_PACKAGE_QNAME,
			REMOVE_ANNOTATION_FROM_CLASS_QNAME,
			REMOVE_ANNOTATION_FROM_ELEMENT_QNAME,
			REMOVE_ANNOTATION_FROM_PROPERTY_QNAME,
			REMOVE_ANNOTATION_FROM_PROPERTY_FIELD_QNAME,
			REMOVE_ANNOTATION_FROM_PROPERTY_GETTER_QNAME,
			REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_QNAME,
			REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_PARAMETER_QNAME,
			REMOVE_ANNOTATION_FROM_ENUM_QNAME,
			REMOVE_ANNOTATION_FROM_ENUM_CONSTANT_QNAME,
			REMOVE_ANNOTATION_FROM_ENUM_VALUE_METHOD_QNAME,
			REMOVE_ANNOTATION_FROM_ENUM_FROM_VALUE_METHOD_QNAME)));
	
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
		return CUSTOMIZATION_ELEMENT_QNAMES.contains(name);
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
		
		return true;
	}

	protected void processElementOutline(ElementOutline elementOutline) throws SAXParseException
	{
		final CCustomizations customizations = CustomizationUtils.getCustomizations(elementOutline);
		removeAnnotationFromElementOutline(elementOutline, customizations);
	}
	
	protected void removeAnnotationFromElementOutline(final ElementOutline elementOutline, final CCustomizations customizations)
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
					if ( removeAnnotation(theElementOwner, customization, element, annotatable) )
					{
						debug("{}, removeAnnotationFromElementOutline; Class={}",
							toLocation(customization, elementOutline), theElementClass.name());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					getLogger().error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	protected void processClassOutline(ClassOutline classOutline) throws SAXParseException
	{
		final CCustomizations customizations = CustomizationUtils.getCustomizations(classOutline);
		removeAnnotationFromClassOutline(classOutline, customizations);

		for (final FieldOutline fieldOutline : classOutline.getDeclaredFields())
			processFieldOutline(classOutline, fieldOutline);
	}

	protected void removeAnnotationFromClassOutline(final ClassOutline classOutline, final CCustomizations customizations)
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
					if ( removeAnnotation(theClassRefOwner, customization, element, annotatable) )
					{
						debug("{}, removeAnnotationFromClassOutline; Class={}",
							toLocation(customization, classOutline), theClassRef.name());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					getLogger().error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	protected void processFieldOutline(ClassOutline classOutline, FieldOutline fieldOutline)
		throws SAXParseException
	{
		final CCustomizations customizations = CustomizationUtils.getCustomizations(fieldOutline);
		removeAnnotationFromFieldOutline(fieldOutline, customizations);
	}

	protected void removeAnnotationFromFieldOutline(final FieldOutline fieldOutline, final CCustomizations customizations)
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
					if ( removeAnnotation(fieldParentRefOwner, customization, element, annotatable) )
					{
						trace("{}, removeAnnotationFromFieldOutline; Class={}, Field={}",
							toLocation(customization, fieldOutline), fieldParentRef.name(), fieldInfo.getName(false));
					}
				}
				catch (IllegalArgumentException iaex)
				{
					getLogger().error("Error removing the annotation.", iaex);
				}
			}
		}
	}

	protected void processEnumOutline(EnumOutline enumOutline) throws SAXParseException
	{
		final CCustomizations customizations = CustomizationUtils.getCustomizations(enumOutline);
		removeAnnotationFromEnumOutline(enumOutline, customizations);

		for (final EnumConstantOutline enumConstantOutline : enumOutline.constants)
			processEnumConstantOutline(enumOutline, enumConstantOutline);
	}

	protected void removeAnnotationFromEnumOutline(final EnumOutline enumOutline, final CCustomizations customizations)
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
					final JAnnotatable annotatable = annotationTarget.getAnnotatable(enumOutline.parent(), enumOutline);
					if ( removeAnnotation(theEnumClassOwner, customization, element, annotatable) )
					{
						debug("{}, removeAnnotationFromEnumOutline; Enum={}",
							toLocation(customization, enumOutline), theEnumClass.name());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					getLogger().error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	protected void processEnumConstantOutline(EnumOutline enumOutline, EnumConstantOutline enumConstantOutline)
		throws SAXParseException
	{
		final CCustomizations customizations = CustomizationUtils.getCustomizations(enumConstantOutline);
		removeAnnotationFromEnumConstantOutline(enumOutline, enumConstantOutline, customizations);
	}

	protected void removeAnnotationFromEnumConstantOutline(final EnumOutline enumOutline,
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
					final JAnnotatable annotatable = annotationTarget.getAnnotatable(enumOutlineParent, enumConstantOutline);
					if ( removeAnnotation(enumOutlineParentOwner, customization, element, annotatable) )
					{
						trace("{}, removeAnnotationFromEnumConstantOutline; Class={}, EnumConstant={}",
							toLocation(customization, enumOutline), enumClass.name(), enumConstantOutline.constRef.getName());
					}
				}
				catch (IllegalArgumentException iaex)
				{
					getLogger().error("Error applying the annotation.", iaex);
				}
			}
		}
	}

	private boolean removeAnnotation(final JCodeModel codeModel, final CPluginCustomization customization,
		final Element element, final JAnnotatable annotatable) throws SAXParseException
	{
		boolean removed = false;
		final String aClass = element.getAttribute(CLASS_ATTRIBUTE_NAME);
		if (isBlank(aClass))
		{
			StringBuilder msg = new StringBuilder();
			msg.append("Could not remove the annotation, annotation class is not specified. ");
			msg.append("Annotation class must be specified using the class attribute of the customization element.");
			throw new SAXParseException(msg.toString(), customization.locator);
		}
		else
		{
			JClass annotationClass = codeModel.ref(aClass);
			
			JAnnotationUse annotationUse = null;
			for (JAnnotationUse annotation : annotatable.annotations())
			{
				if (annotationClass.equals(annotation.getAnnotationClass()))
					annotationUse = annotation;
			}
			
			if (annotationUse == null)
			{
				String msg = format("Could not remove the annotation, target element is not annotated with annotation class [%s].",
					annotationClass);
				throw new SAXParseException(msg, customization.locator);
			}
			
			// Removes annotation from this program element.
			removed = annotatable.removeAnnotation(annotationUse);
		}
		return removed;
	}
}
