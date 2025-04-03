package org.jvnet.hyperjaxb_annox.plugin;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_CLASS_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_ELEMENT_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_ENUM_CONSTANT_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_ENUM_FROM_VALUE_METHOD_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_ENUM_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_ENUM_VALUE_METHOD_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_PACKAGE_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_OBJECT_FACTORY_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_PROPERTY_FIELD_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_PROPERTY_GETTER_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_PROPERTY_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_PROPERTY_SETTER_PARAMETER_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_PROPERTY_SETTER_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.annotate.AnnotatePlugin.ANNOTATE_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_CLASS_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_ELEMENT_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_ENUM_CONSTANT_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_ENUM_FROM_VALUE_METHOD_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_ENUM_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_ENUM_VALUE_METHOD_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_OBJECT_FACTORY_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_PACKAGE_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_PROPERTY_FIELD_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_PROPERTY_GETTER_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_PROPERTY_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_PARAMETER_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_QNAME;
import static org.jvnet.hyperjaxb_annox.plugin.removeannotation.RemoveAnnotationPlugin.REMOVE_ANNOTATION_QNAME;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jvnet.basicjaxb.util.FieldAccessorUtils;
import org.jvnet.basicjaxb.util.OutlineUtils;
import org.w3c.dom.Element;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.ElementOutline;
import com.sun.tools.xjc.outline.EnumConstantOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * This <code>enum</code> declares the possible targets for the <code>annotate</code>
 * and <code>removeAnnotate</code> bindings. Each target enumeration provides methods
 * to get an {@link JAnnotatable } instance from the XJC outline model. This
 * <code>enum</code>s <em>default "unsupported"</em> methods are customized by each
 * enumeration, as appropriate.
 */
public enum AnnotationTarget
{
	// Target enumerations with methods to get an annotatable.
	
	PACKAGE("package", ANNOTATE_PACKAGE_QNAME, REMOVE_ANNOTATION_FROM_PACKAGE_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		{
			return fieldOutline.parent().ref._package();
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, ClassOutline classOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return classOutline.ref._package();
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, ElementOutline elementOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return elementOutline.implClass._package();
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumConstantOutline enumConstantOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			final CEnumLeafInfo enclosingClass = enumConstantOutline.target.getEnclosingClass();
			return getAnnotatable(outline, outline.getEnum(enclosingClass));
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumOutline enumOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return enumOutline.clazz._package();
		}
	},
	OBJECT_FACTORY("objectFactory", ANNOTATE_OBJECT_FACTORY_QNAME, REMOVE_ANNOTATION_FROM_OBJECT_FACTORY_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		{
			return fieldOutline.parent()._package().objectFactory();
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, ClassOutline classOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return classOutline._package().objectFactory();
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, ElementOutline elementOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return elementOutline._package().objectFactory();
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumConstantOutline enumConstantOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			final CEnumLeafInfo enclosingClass = enumConstantOutline.target.getEnclosingClass();
			return getAnnotatable(outline, outline.getEnum(enclosingClass));
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumOutline enumOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return enumOutline._package().objectFactory();
		}
	},
	CLASS("class", ANNOTATE_CLASS_QNAME, REMOVE_ANNOTATION_FROM_CLASS_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		{
			return fieldOutline.parent().ref;
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, ClassOutline classOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return classOutline.ref;
		}
	},
	PROPERTY_GETTER("getter", ANNOTATE_PROPERTY_GETTER_QNAME, REMOVE_ANNOTATION_FROM_PROPERTY_GETTER_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		{
			final JMethod _getter = FieldAccessorUtils.getter(fieldOutline);
			if (_getter == null)
			{
				throw new IllegalArgumentException(format(
					"Could not annotate the getter of the field outline [%s], getter method could not be found.",
					OutlineUtils.getFieldName(fieldOutline)));
			}
			return _getter;
		}
	},
	PROPERTY_SETTER("setter", ANNOTATE_PROPERTY_SETTER_QNAME, REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		{
			final JMethod _setter = FieldAccessorUtils.setter(fieldOutline);
			if (_setter == null)
			{
				throw new IllegalArgumentException(format(
					"Could not annotate the setter of the field outline [%s], setter method could not be found.",
					OutlineUtils.getFieldName(fieldOutline)));
			}
			return _setter;
		}
	},
	PROPERTY_FIELD("field", ANNOTATE_PROPERTY_FIELD_QNAME, REMOVE_ANNOTATION_FROM_PROPERTY_FIELD_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		{
			final JFieldVar _field = FieldAccessorUtils.field(fieldOutline);
			if (_field == null)
			{
				throw new IllegalArgumentException(format(
					"Could not annotate the field of the field outline [%s] since it could not be found.",
					OutlineUtils.getFieldName(fieldOutline)));
			}
			return _field;
		}
	},
	PROPERTY_SETTER_PARAMETER("setter-parameter", ANNOTATE_PROPERTY_SETTER_PARAMETER_QNAME, REMOVE_ANNOTATION_FROM_PROPERTY_SETTER_PARAMETER_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		{
			final JMethod _setter = FieldAccessorUtils.setter(fieldOutline);
			if (_setter == null)
			{
				throw new IllegalArgumentException(format(
					"Could not annotate the setter parameter of the field outline [%s], setter method could not be found.",
					OutlineUtils.getFieldName(fieldOutline)));
			}
			else
			{
				final JVar[] params = _setter.listParams();
				if (params.length != 1)
				{
					throw new IllegalArgumentException(format(
						"Could not annotate the setter parameter of the field outline [%s], setter method must have a single parameter(this setter has {1}).",
						OutlineUtils.getFieldName(fieldOutline), params.length));
				}
				else
				{
					return params[0];
				}
			}
		}
	},
	ENUM("enum", ANNOTATE_ENUM_QNAME, REMOVE_ANNOTATION_FROM_ENUM_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumConstantOutline enumConstantOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			final CEnumLeafInfo enclosingClass = enumConstantOutline.target.getEnclosingClass();
			return getAnnotatable(outline, outline.getEnum(enclosingClass));
		}

		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumOutline enumOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return enumOutline.clazz;
		}
	},
	ENUM_CONSTANT("enum-constant", ANNOTATE_ENUM_CONSTANT_QNAME, REMOVE_ANNOTATION_FROM_ENUM_CONSTANT_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumConstantOutline enumConstantOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return enumConstantOutline.constRef;
		}
	},
	ENUM_VALUE_METHOD("enum-value-method", ANNOTATE_ENUM_VALUE_METHOD_QNAME, REMOVE_ANNOTATION_FROM_ENUM_VALUE_METHOD_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumOutline enumOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			final JMethod valueMethod = enumOutline.clazz.getMethod("value", new JType[0]);
			if (null == valueMethod)
			{
				throw new IllegalArgumentException(format(
					"Could not annotate the value() method of the enum [%s] since it could not be found.",
					enumOutline.clazz.name()));
			}
			return valueMethod;
		}
	},
	ENUM_FROM_VALUE_METHOD("enum-fromValue-method", ANNOTATE_ENUM_FROM_VALUE_METHOD_QNAME, REMOVE_ANNOTATION_FROM_ENUM_FROM_VALUE_METHOD_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, EnumOutline enumOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			final JCodeModel codeModel = enumOutline.clazz.owner();
			final JType jTypeString = codeModel._ref(String.class);
			final JMethod fromValueMethod = enumOutline.clazz.getMethod("fromValue", new JType[] { jTypeString });
			if (null == fromValueMethod)
			{
				throw new IllegalArgumentException(format(
					"Could not annotate the fromValue(String) method of the enum [%s] since it could not be found.",
					enumOutline.clazz.name()));
			}
			return fromValueMethod;
		}
	},
	ELEMENT("element", ANNOTATE_ELEMENT_QNAME, REMOVE_ANNOTATION_FROM_ELEMENT_QNAME)
	{
		@Override
		public JAnnotatable getAnnotatable(Outline outline, ElementOutline elementOutline)
			throws IllegalArgumentException, UnsupportedOperationException
		{
			return elementOutline.implClass;
		}
	};

	// Property: Represents the target value as a String.
	private final String target;
	public String getTarget()
	{
		return target;
	}

	// Property: Represents the qualified name(s).
	private final Set<QName> names;
	public Set<QName> getNames()
	{
		return names;
	}

	/* Constructor: Construct by target and {@link QName}(s). */
	private AnnotationTarget(String target, QName... names)
	{
		this.target = target;
		this.names = Collections.unmodifiableSet(new HashSet<QName>(Arrays.asList(names)));
	}

	// EnumOutline: Each target optionally overrides this method, as appropriate.
	public JAnnotatable getAnnotatable(Outline outline, EnumOutline enumOutline)
		throws IllegalArgumentException, UnsupportedOperationException
	{
		throw newUnsupportedOperationException("an enum");
	}

	// EnumConstantOutline: Each target optionally overrides this method, as appropriate.
	public JAnnotatable getAnnotatable(Outline outline, EnumConstantOutline enumConstantOutline)
		throws IllegalArgumentException, UnsupportedOperationException
	{
		throw newUnsupportedOperationException("an enum constant");
	}

	// ClassOutline: Each target optionally overrides this method, as appropriate.
	public JAnnotatable getAnnotatable(Outline outline, ClassOutline classOutline)
		throws IllegalArgumentException, UnsupportedOperationException
	{
		throw newUnsupportedOperationException("a class");
	}

	// FieldOutline: Each target optionally overrides this method, as appropriate.
	public JAnnotatable getAnnotatable(Outline outline, FieldOutline fieldOutline)
		throws IllegalArgumentException, UnsupportedOperationException
	{
		throw newUnsupportedOperationException("a field");
	}

	// ElementOutline: Each target optionally overrides this method, as appropriate.
	public JAnnotatable getAnnotatable(Outline outline, ElementOutline elementOutline)
		throws IllegalArgumentException, UnsupportedOperationException
	{
		throw newUnsupportedOperationException("an element");
	}

	// Create exception for unsupported target and/or type.
	private UnsupportedOperationException newUnsupportedOperationException(String type)
	{
		String msg = format("Annotation target [%s] cannot be applied to %s.", getTarget(), type);
		return new UnsupportedOperationException(msg);
	}
	
	/**
	 * <p>Get the {@link AnnotationTarget} for the given {@link Element} value with the
	 * given default {@link AnnotationTarget}.</p>
	 * 
	 * <b>Element examples:</b>
	 * <pre>{@code
	 * {http://jvnet.org/basicjaxb/xjc/annox}annotate
	 * {http://jvnet.org/basicjaxb/xjc/annox}annotateElement
	 * {http://jvnet.org/basicjaxb/xjc/annox}annotateClass
	 * {http://jvnet.org/basicjaxb/xjc/annox}annotatePropertyField
	 * {http://jvnet.org/basicjaxb/xjc/annox}removeAnnotate
	 * }</pre>
	 * 
	 * @param element An element representing an {@code annotate} or {@code removeAnnotate} action.
	 * @param defaultAnnotationTarget The default target when none is specified.
	 * 
	 * @return The {@link AnnotationTarget} enumeration for the given element/default.
	 * 
	 * @throws IllegalArgumentException when the annotation/removeAnnotation value is unknown.
	 */
	public static AnnotationTarget getAnnotationTarget(final Element element, AnnotationTarget defaultAnnotationTarget)
	{
		requireNonNull(element);
		requireNonNull(defaultAnnotationTarget);
		
		final QName name = new QName(element.getNamespaceURI(), element.getLocalName());
		if ( ANNOTATE_QNAME.equals(name) || ANNOTATE_PROPERTY_QNAME.equals(name) ||
			 REMOVE_ANNOTATION_QNAME.equals(name) || REMOVE_ANNOTATION_FROM_PROPERTY_QNAME.equals(name))
		{
			final String target = element.getAttribute("target");
			if ( isBlank(target) )
				return defaultAnnotationTarget;
			else
				return getAnnotationTarget(target);
		}
		else
		{
			for (AnnotationTarget possibleAnnotationTarget : values())
			{
				if (possibleAnnotationTarget.getNames().contains(name))
					return possibleAnnotationTarget;
			}
			throw new IllegalArgumentException(format("Unknown annotation element name [%s].", name));
		}
	}

	/**
	 * Get the {@link AnnotationTarget} for the given target value.
	 * 
	 * @param target The target value representing an {@link AnnotationTarget} enumeration.
	 * 
	 * @return The {@link AnnotationTarget} enumeration for the given target.
	 * 
	 * @throws IllegalArgumentException when the target value is unknown.
	 */
	public static AnnotationTarget getAnnotationTarget(final String target)
	{
		// Compare each enumeration target to the given target.
		for (AnnotationTarget possibleAnnotationTarget : AnnotationTarget.values())
		{
			if ( possibleAnnotationTarget.getTarget().equals(target) )
				return possibleAnnotationTarget;
		}
		
		// Throw exception when the target cannot be matched.
		throw new IllegalArgumentException(format("Unknown annotation target [%s].", target));
	}
}
