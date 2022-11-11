package org.jvnet.hyperjaxb_annox.plugin.annotate;

import org.jvnet.basicjaxb_annox.model.XAnnotation;
import org.jvnet.basicjaxb_annox.model.annotation.field.XAnnotationField;
import org.jvnet.basicjaxb_annox.model.annotation.value.XAnnotationValueVisitor;
import org.jvnet.basicjaxb_annox.model.annotation.value.XArrayClassAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XBooleanAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XByteAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XCharAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XClassAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XClassByNameAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XDoubleAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XEnumAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XEnumByNameAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XFloatAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XIntAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XLongAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XShortAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XStringAnnotationValue;
import org.jvnet.basicjaxb_annox.model.annotation.value.XXAnnotationAnnotationValue;
import org.jvnet.basicjaxb.util.CodeModelUtils;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JType;

public class AnnotatingArrayValueVisitor implements
		XAnnotationValueVisitor<JAnnotationArrayMember> {

	private final JCodeModel codeModel;
	private final JAnnotationArrayMember annotationArrayMember;

	public AnnotatingArrayValueVisitor(JCodeModel codeModel,
			JAnnotationArrayMember annotationArrayMember) {
		this.codeModel = codeModel;
		this.annotationArrayMember = annotationArrayMember;
	}

//	public JAnnotationArrayMember visit(XAnnotationAnnotationValue<?> value) {
//
//		final XAnnotation<?> xannotation = value.getXAnnotation();
//		final JAnnotationUse annotationUse = annotationArrayMember
//				.annotate(xannotation.getAnnotationClass());
//
//		for (final XAnnotationField<?> field : xannotation.getFieldsList()) {
//			field.accept(new AnnotatingVisitor(this.codeModel, annotationUse));
//		}
//
//		return this.annotationArrayMember;
//	}

	public JAnnotationArrayMember visit(XXAnnotationAnnotationValue<?> value) {
		final XAnnotation<?> xannotation = value.getXAnnotation();
		// TODO illegal call to getAnnotationClass(), use
		// getAnnotationClassName();
		final JAnnotationUse annotationUse = annotationArrayMember
				.annotate(xannotation.getAnnotationClass());

		for (final XAnnotationField<?> field : xannotation.getFieldsList()) {
			field.accept(new AnnotatingVisitor(this.codeModel, annotationUse));
		}

		return this.annotationArrayMember;
	}

	public JAnnotationArrayMember visit(XBooleanAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XByteAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XCharAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XDoubleAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XFloatAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XIntAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XLongAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XShortAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XStringAnnotationValue value) {
		return annotationArrayMember.param(value.getValue());
	}

	public JAnnotationArrayMember visit(XEnumAnnotationValue<?> value) {
		final Enum<?> e = value.getValue();
		return annotationArrayMember.param(e);
	}

	public JAnnotationArrayMember visit(XEnumByNameAnnotationValue<?> value) {
		final JClass type = (JClass) CodeModelUtils.ref(this.codeModel,
				value.getEnumClassName());
		return annotationArrayMember.param(type.staticRef(value.getName()));
	}

	public JAnnotationArrayMember visit(XClassAnnotationValue<?> value) {
		final JType type = this.codeModel._ref(value.getValue());
		return param(type);
	}

	public JAnnotationArrayMember visit(XClassByNameAnnotationValue<?> value) {
		final JType ref = CodeModelUtils.ref(codeModel, value.getClassName());
		return param(ref);
	}

	public JAnnotationArrayMember visit(XArrayClassAnnotationValue<?, ?> value) {
		JType type = CodeModelUtils.ref(this.codeModel,
				value.getItemClassName());
		for (int index = 0; index < value.getDimension(); index++) {
			type = type.array();
		}
		return param(type);
	}

	private JAnnotationArrayMember param(final JType type) {
		if (type instanceof JClass) {
			return annotationArrayMember.param((JClass) type);
		} else {
			return annotationArrayMember.param(new JExpressionImpl() {
				public void generate(JFormatter f) {
					f.g(type).p(".class");
				}
			});
		}
	}

}
