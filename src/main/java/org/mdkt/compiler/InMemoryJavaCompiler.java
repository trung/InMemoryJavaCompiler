package org.mdkt.compiler;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by trung on 5/3/15.
 */
public class InMemoryJavaCompiler {
    static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    public static Class<?> compile(String className, String sourceCodeInText) throws Exception {
        SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
        CompiledCode compiledCode = new CompiledCode(className);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceCode);
        DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCode, cl);
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, null, null, null, compilationUnits);
        boolean result = task.call();
        return cl.loadClass(className);
    }

    public static Class<?>[] mutilCompile(String[] classNames, String[] sourceCodeInTexts) throws Exception {
        if (javac == null) {
            throw new Exception("ToolProvider.getSystemJavaCompiler()==null,java.home = "
                    + System.getProperty("java.home") + " try to find tool.jar");
        }
        if (classNames.length != sourceCodeInTexts.length) {
            throw new Exception("classNames length != sourceCodeInTexts length!");
        }
        List<SourceCode> compilationUnits = new ArrayList<SourceCode>();
        CompiledCode compiledCodes[] = new CompiledCode[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            String sourceCodeInText = sourceCodeInTexts[i];
            SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
            compiledCodes[i] = new CompiledCode(className);
            compilationUnits.add(sourceCode);
        }
        DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null),
                compiledCodes, cl);
        JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, null, null, null, compilationUnits);
        boolean result = task.call();
        if (!result) {
            throw new Exception("Compile error！！！！");
        }
        Map<String, CompiledCode> map = cl.getCustomCompiledCode();
        List<Class<?>> list = new ArrayList<Class<?>>();
        for (String key : map.keySet()) {
            list.add(cl.findClass(key));
        }
        return list.toArray(new Class<?>[map.keySet().size()]);
    }
}
