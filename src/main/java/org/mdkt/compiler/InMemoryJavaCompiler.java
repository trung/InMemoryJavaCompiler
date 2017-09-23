package org.mdkt.compiler;

import javax.tools.*;
import java.util.*;

/**
 * Created by trung on 5/3/15.
 */
public class InMemoryJavaCompiler {
    private static       JavaCompiler     javac   = ToolProvider.getSystemJavaCompiler();
    private static final Iterable<String> options = Collections.singletonList("-Xlint:unchecked");
    private DiagnosticCollector<JavaFileObject> collector;

    public InMemoryJavaCompiler() {
        this.collector = new DiagnosticCollector<>();
    }

    public Class<?> compile(String className, String sourceCodeInText) throws Exception {
        SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
        CompiledCode compiledCode = new CompiledCode(className);
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(sourceCode);
        DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCode, cl);

        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector,
                                                          options, null, compilationUnits);
        try {
            boolean result = task.call();

            if(!result || collector.getDiagnostics().size() > 0) {
                throw new InMemoryCompilerException(collector.getDiagnostics());
            }
        } catch(ClassFormatError e) {
            throw new InMemoryCompilerException(collector.getDiagnostics());
        }

        return cl.loadClass(className);
    }
}
