package org.mdkt.compiler;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

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

        InMemoryJavaCompiler inMemoryJavaCompiler = new InMemoryJavaCompiler();
        Class<?> helloClass = inMemoryJavaCompiler.compile("org.mdkt.HelloClass", sourceCode.toString());
        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }


    @Test
    public void compile_severalFiles() throws Exception {
        String cls1 = "public class A{ public B b() { return new B(); }}";
        String cls2 = "public class B{ public String toString() { return \"B!\"; }}";

        InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();
        compiler.addSource("A", cls1);
        compiler.addSource("B", cls2);
        Map<String, Class<?>> compiled = compiler.compileAll();
        ;
        Assert.assertNotNull(compiled.get("A"));
        Assert.assertNotNull(compiled.get("B"));

        Class<?> aClass = compiled.get("A");
        Object a = aClass.newInstance();
        Assert.assertEquals("B!", aClass.getMethod("b").invoke(a).toString());
    }


    @Test
    public void compile_filesWithInnerClasses() throws Exception {
        StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package org.mdkt;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   private static class InnerHelloWorld { int inner; }\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");

        InMemoryJavaCompiler inMemoryJavaCompiler = new InMemoryJavaCompiler();
        Class<?> helloClass = inMemoryJavaCompiler.compile("org.mdkt.HelloClass", sourceCode.toString());
        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }
}
