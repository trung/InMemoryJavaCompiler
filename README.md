**Update 09/22/2017**: I've been silent for long time (I'm into Golang lately hence putting Java aside) despite the fact that there are lots of interests to make this mini tool better. I'll kick off my effort to improve this tool from now on by reviewing outstanding issues and PRs

# InMemoryJavaCompiler [![Build Status](https://travis-ci.org/trung/InMemoryJavaCompiler.svg?branch=master)](https://travis-ci.org/trung/InMemoryJavaCompiler)
Samples with utility classes to compile java source code in memory

After taking huge effort to look for example on the internet and found nothing work. I decided to create a very simple version.

**Note**: Please make sure you use JDK in your runtime

E.g.:

    StringBuffer sourceCode = new StringBuffer();
    sourceCode.append("package org.mdkt;\n");
    sourceCode.append("public class HelloClass {\n");
    sourceCode.append("   public String hello() { return \"hello\"; }");
    sourceCode.append("}");

    Class<?> helloClass = InMemoryJavaCompiler.compile("org.mdkt.HelloClass", sourceCode.toString());

Alternatively, if you want to load several (dependent) classes:

    String cls1 = "public class A{ public B b() { return new B(); }}";
    String cls2 = "public class B{ public String toString() { return \"B!\"; }}";
        
    InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();
    compiler.addSource("A", cls1);
    compiler.addSource("B", cls2);
    Map<String,Class<?>> compiled = compiler.compileAll();
    
    Class<?> aClass = compiled.get("A");
    

Artifact is pushed to Sonatype OSS Releases Repository

    https://oss.sonatype.org/content/repositories/releases/

Maven dependency:

    <dependency>
        <groupId>org.mdkt.compiler</groupId>
        <artifactId>InMemoryJavaCompiler</artifactId>
        <version>1.2</version>
    </dependency>
