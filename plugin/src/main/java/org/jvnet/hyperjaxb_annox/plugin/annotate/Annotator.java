package org.jvnet.hyperjaxb_annox.plugin.annotate;

import java.util.Collection;

import org.jvnet.basicjaxb_annox.model.XAnnotation;
import org.jvnet.basicjaxb_annox.model.XAnnotationFieldVisitor;
import org.jvnet.basicjaxb_annox.model.annotation.field.XAnnotationField;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationStringValue;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;

public class Annotator
{
	public static final Annotator INSTANCE = new Annotator();

	public void annotate(JCodeModel codeModel, JAnnotatable annotatable,
		Collection<XAnnotation<?>> xannotations)
	{
		for ( final XAnnotation<?> xannotation : xannotations )
		{
			if ( xannotation != null )
				annotate(codeModel, annotatable, xannotation);
		}
	}

	public void annotate(JCodeModel codeModel, JAnnotatable annotatable,
		XAnnotation<?> xannotation)
	{
		final JClass xannotationClass = codeModel.ref(xannotation.getAnnotationClass());
		final XAnnotationField<?> xaNameField = xannotation.getFieldsMap().get("name");

		// Resolve any existing annotation for merging.
		JAnnotationUse annotationUse = null;
		for ( JAnnotationUse annotation : annotatable.annotations() )
		{
			// Resolved annotations must equate on class.
			if ( xannotationClass.equals(annotation.getAnnotationClass()) )
			{
				// Named annotations must equate on name, also.
				if ( xaNameField != null )
				{
					JAnnotationValue aaNameValue = annotation.getAnnotationMembers().get("name");
					if ( aaNameValue instanceof JAnnotationStringValue aaNameStringValue )
					{
						// When both sides are the same class but have different
						// names; do not de-duplicate.
						String xaNameString = (String) xaNameField.getValue();
						// Both sides have a name field, check values for equality.
						if ( xaNameString != null )
						{
							String aaNameString = aaNameStringValue.toString();
							if ( xaNameString.equals(aaNameString) )
								annotationUse = annotation;
						}
					}
					else
						annotationUse = annotation;
				}
				else
					annotationUse = annotation;
			}
		}

		// If new, add this xannotationClass annotation to this program element;
		// otherwise merge fields with the resolved (existing) annotation.
		if ( annotationUse == null )
			annotationUse = annotatable.annotate(xannotationClass);

		final XAnnotationFieldVisitor<?> visitor =
			createAnnotationFieldVisitor(codeModel, annotationUse);
		for ( XAnnotationField<?> field : xannotation.getFieldsList() )
			field.accept(visitor);
	}

	protected XAnnotationFieldVisitor<?> createAnnotationFieldVisitor(JCodeModel codeModel,
		final JAnnotationUse annotationUse)
	{
		final XAnnotationFieldVisitor<?> visitor = new AnnotatingVisitor(codeModel, annotationUse);
		return visitor;
	}
}
