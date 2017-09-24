package org.mdkt.compiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * Complile Java sources in-memory
 */
public class InMemoryJavaCompiler {
	private JavaCompiler javac;
	private DynamicClassLoader classLoader;

	private Map<String, SourceCode> sourceCodes = new HashMap<String, SourceCode>();

	public static InMemoryJavaCompiler newInstance() {
		return new InMemoryJavaCompiler();
	}

	private InMemoryJavaCompiler() {
		this.javac = ToolProvider.getSystemJavaCompiler();
		this.classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
	}

	public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
		this.classLoader = new DynamicClassLoader(parent);
		return this;
	}

	/**
	 * Compile all sources
	 *
	 * @return
	 * @throws Exception
	 */
	public Map<String, Class<?>> compileAll() throws Exception {
		if (sourceCodes.size() == 0) {
			throw new Exception("No source code to compile");
		}
		Collection<SourceCode> compilationUnits = sourceCodes.values();
		CompiledCode[] code;

		code = new CompiledCode[compilationUnits.size()];
		Iterator<SourceCode> iter = compilationUnits.iterator();
		for (int i = 0; i < code.length; i++) {
			code[i] = new CompiledCode(iter.next().getClassName());
		}
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, null, null, compilationUnits);
		boolean result = task.call();
		if (!result || collector.getDiagnostics().size() > 0) {
			throw new CompilationException(collector.getDiagnostics());
		}

		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
		for (String className : sourceCodes.keySet()) {
			classes.put(className, classLoader.loadClass(className));
		}
		return classes;
	}

	/**
	 * Compile single source
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 */
	public Class<?> compile(String className, String sourceCode) throws Exception {
		return addSource(className, sourceCode).compileAll().get(className);
	}

	/**
	 * Add source code to the compiler
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 * @see {@link #compileAll()}
	 */
	public InMemoryJavaCompiler addSource(String className, String sourceCode) throws Exception {
		sourceCodes.put(className, new SourceCode(className, sourceCode));
		return this;
	}
}
