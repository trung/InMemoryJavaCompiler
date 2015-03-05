# InMemoryJavaCompiler
Samples with utility classes to compile java source code in memory

After taking huge effort to look for example on the internet and found nothing work. I decided to create a very simple version.

E.g.:

    StringBuffer sourceCode = new StringBuffer();
    sourceCode.append("package org.mdkt;\n");
    sourceCode.append("public class HelloClass {\n");
    sourceCode.append("   public String hello() { return \"hello\"; }");
    sourceCode.append("}");

    Class<?> helloClass = InMemoryJavaCompiler.compile("org.mdkt.HelloClass", sourceCode.toString());
