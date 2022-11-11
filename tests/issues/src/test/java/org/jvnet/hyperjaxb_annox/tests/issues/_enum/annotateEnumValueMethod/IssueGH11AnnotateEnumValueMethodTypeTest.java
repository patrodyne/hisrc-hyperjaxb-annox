package org.jvnet.hyperjaxb_annox.tests.issues._enum.annotateEnumValueMethod;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.jvnet.hyperjaxb_annox.plugin.annotate.tests.annotations.Marked;

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
