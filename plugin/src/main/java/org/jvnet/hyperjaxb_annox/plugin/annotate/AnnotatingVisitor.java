package org.jvnet.hyperjaxb_annox.plugin.annotate;

import org.jvnet.basicjaxb_annox.model.XAnnotationFieldVisitor;
import org.jvnet.basicjaxb_annox.model.annotation.field.XArrayAnnotationField;
import org.jvnet.basicjaxb_annox.model.annotation.field.XSingleAnnotationField;
import org.jvnet.basicjaxb_annox.model.annotation.value.XAnnotationValue;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;

public class AnnotatingVisitor implements
		XAnnotationFieldVisitor<JAnnotationUse> {

	private final JCodeModel codeModel;
	private final JAnnotationUse annotationUse;

	public AnnotatingVisitor(JCodeModel codeModel, JAnnotationUse annotationUse) {
		this.codeModel = codeModel;
		this.annotationUse = annotationUse;
	}

	@Override
	public JAnnotationUse visitSingleAnnotationField(
			XSingleAnnotationField<?> field) {
		final XAnnotationValue<?> annotationValue = field.getAnnotationValue();
		annotationValue.accept(new AnnotatingSingleValueVisitor(this.codeModel,
				field.getName(), this.annotationUse));
		return this.annotationUse;
	}

	@Override
	public JAnnotationUse visitArrayAnnotationField(
			XArrayAnnotationField<?> field) {

		String fieldName = field.getName();
		final JAnnotationArrayMember annotationArrayMember = this.annotationUse
				.paramArray(fieldName);

		for (final XAnnotationValue<?> annotationValue : field
				.getAnnotationValues()) {
			annotationValue.accept(new AnnotatingArrayValueVisitor(
					this.codeModel, annotationArrayMember));
		}
		return this.annotationUse;
	}

}
