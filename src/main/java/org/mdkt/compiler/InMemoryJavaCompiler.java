package org.mdkt.compiler;

import javax.tools.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by trung on 5/3/15.
 * Changed by PKeidel on 13.10.15.
 */
public class InMemoryJavaCompiler {
    static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    public static Class<?> compile(String className, String sourceCodeInText) throws Exception {
        final DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
        CompiledCode compiledCode = new CompiledCode(className);
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(sourceCode);
        DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(diagnosticsCollector, null, null), compiledCode, cl);
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, diagnosticsCollector, null, null, compilationUnits);
        boolean result = task.call();
        fileManager.close();

        if (!result) {
            StringBuilder sb = new StringBuilder();
            List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                // read error dertails from the diagnostic object
                sb.append("=> ").append(diagnostic.getMessage(null)).append("\n");
            }
            throw new CompileException(sb.toString());
        }

        return cl.loadClass(className);
    }
}
