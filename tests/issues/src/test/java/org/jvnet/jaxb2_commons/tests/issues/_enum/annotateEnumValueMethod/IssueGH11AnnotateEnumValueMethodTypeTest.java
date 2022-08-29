package org.jvnet.jaxb2_commons.tests.issues._enum.annotateEnumValueMethod;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;
import org.jvnet.jaxb2_commons.plugin.annotate.tests.annotations.Marked;

public class IssueGH11AnnotateEnumValueMethodTypeTest {

    @Test
    public void valueMethodShouldBeAnnotated() throws Exception {
        String expectedValue = "annotateEnumValueMethod";

        Method valueMethod = IssueGH11AnnotateEnumValueMethodType.class.getDeclaredMethod("value");
        Marked actualAnnotation = valueMethod.getAnnotation(Marked.class);

        assertThat(actualAnnotation, is(notNullValue()));
        assertThat(actualAnnotation.value(), is(equalTo(expectedValue)));
    }
}
