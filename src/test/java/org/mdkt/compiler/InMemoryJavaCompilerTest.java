package org.mdkt.compiler;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by trung on 5/3/15.
 */
public class InMemoryJavaCompilerTest {

    @Test
    public void compile_whenTypical() throws Exception {
        StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");

        Class<?> helloClass = InMemoryJavaCompiler.compile("org.mdkt.HelloClass", sourceCode.toString());
        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }

    @Test
    public void compile_mutil_classes() throws Exception {
        String classNames[] = new String[]{"com.compile.test.A", "com.compile.test.B"};
        String sources[] = new String[]{"package com.compile.test;import com.compile.test.B;public class A{ B b;int i;}",
                "package com.compile.test; public class B{String a;}"};

        Class<?>[] classes = InMemoryJavaCompiler.mutilCompile(classNames, sources);
        Assert.assertNotNull(classes);
        Assert.assertEquals(classes.length, 2);
    }
}
