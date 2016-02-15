package org.mdkt.compiler;

import javax.tools.*;
import java.util.*;

/**
 * Created by trung on 5/3/15.
 */
public class InMemoryJavaCompiler {
    static  JavaCompiler                        javac = ToolProvider.getSystemJavaCompiler();

    public static Class<?> compile(String className, String sourceCodeInText) throws Exception {
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
        CompiledCode compiledCode = new CompiledCode(className);
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(sourceCode);
        DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCode, cl);
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, null,
                                                          null, compilationUnits);
        try {
            boolean result = task.call();

            if(!result) {
                throw new InMemoryCompilerException(collector.getDiagnostics());
            }
        } catch(ClassFormatError e) {
            throw new InMemoryCompilerException(collector.getDiagnostics());
        }

        return cl.loadClass(className);
    }
}
