package org.mdkt.compiler;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by trung on 5/3/15.
 */
public class InMemoryJavaCompilerTest {

    @Test
    public void compile_whenTypical() throws Exception {
        StringBuilder sourceCode = new StringBuilder();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");

        InMemoryJavaCompiler imjc = new InMemoryJavaCompiler();
        Class<?> helloClass = imjc.compile("org.mdkt.HelloClass", sourceCode.toString());
        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }
}
