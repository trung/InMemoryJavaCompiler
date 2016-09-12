package org.mdkt.compiler;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.util.*;

/**
 * Created by trung on 5/3/15.
 */
public class InMemoryJavaCompiler {
    private static final Iterable<String> options = Collections.singletonList("-Xlint:unchecked");
    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    DynamicClassLoader classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
    private DiagnosticCollector<JavaFileObject> collector;

    public InMemoryJavaCompiler() {
        this.collector = new DiagnosticCollector<>();
    }

    Map<String, SourceCode> clazzCode = new HashMap<>();

    public void addSource(String className, String sourceCodeInText)
            throws Exception {
        clazzCode.put(className, new SourceCode(className, sourceCodeInText));
    }

    public Map<String, Class<?>> compileAll() throws Exception {

        Collection<SourceCode> compilationUnits = clazzCode.values();
        List<CompiledCode> compiledCodes = new ArrayList<>();
        Iterator<SourceCode> iterator = compilationUnits.iterator();
        for (SourceCode sourceCode : compilationUnits) {
            compiledCodes.add(new CompiledCode(sourceCode.getClassName()));
        }

        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCodes, classLoader);

        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector,
                options, null, compilationUnits);


        try {
            boolean result = task.call();

            if (!result || collector.getDiagnostics().size() > 0) {
                throw new InMemoryCompilerException(collector.getDiagnostics());
            }

            Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
            for (String className : clazzCode.keySet()) {
                classes.put(className, classLoader.loadClass(className));
            }
            return classes;
        } catch (ClassFormatError e) {
            throw new InMemoryCompilerException(collector.getDiagnostics());
        }

    }


    public Class<?> compile(String className, String sourceCodeInText) throws Exception {
        addSource(className, sourceCodeInText);
        Map<String, Class<?>> compiled = compileAll();
        Class<?> compiledClass = compiled.get(className);
        return compiledClass;
    }


}