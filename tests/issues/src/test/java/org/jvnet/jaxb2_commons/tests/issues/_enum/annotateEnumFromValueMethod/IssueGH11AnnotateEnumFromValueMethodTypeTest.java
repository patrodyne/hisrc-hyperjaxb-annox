package org.jvnet.jaxb2_commons.tests.issues._enum.annotateEnumFromValueMethod;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.jvnet.jaxb2_commons.plugin.annotate.tests.annotations.Marked;

public class IssueGH11AnnotateEnumFromValueMethodTypeTest {

    @Test
    public void fromValueMethodShouldBeAnnotated() throws Exception {
        String expectedValue = "annotateEnumFromValueMethod";

        Method fromValueMethod = IssueGH11AnnotateEnumFromValueMethodType.class.getDeclaredMethod("fromValue", String.class);
        Marked actualAnnotation = fromValueMethod.getAnnotation(Marked.class);

        assertThat(actualAnnotation, is(notNullValue()));
        assertThat(actualAnnotation.value(), is(equalTo(expectedValue)));
    }
}
