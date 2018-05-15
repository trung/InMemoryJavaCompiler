package org.mdkt.compiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * Compile Java sources in-memory
 */
public class InMemoryJavaCompiler {
	private JavaCompiler javac;
	private DynamicClassLoader classLoader;
	private Iterable<String> options;
	boolean ignoreWarnings = false;

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
	 * @return the class loader used internally by the compiler
	 */
	public ClassLoader getClassloader() {
		return classLoader;
	}

	/**
	 * Options used by the compiler, e.g. '-Xlint:unchecked'.
	 *
	 * @param options
	 * @return
	 */
	public InMemoryJavaCompiler useOptions(String... options) {
		this.options = Arrays.asList(options);
		return this;
	}

	/**
	 * Ignore non-critical compiler output, like unchecked/unsafe operation
	 * warnings.
	 *
	 * @return
	 */
	public InMemoryJavaCompiler ignoreWarnings() {
		ignoreWarnings = true;
		return this;
	}

	/**
	 * Compile all sources
	 *
	 * @return Map containing instances of all compiled classes
	 * @throws Exception
	 */
	public Map<String, Class<?>> compileAll() throws Exception {
		return compile().checkNoErrors().classMap();
	}

	/**
	 * Compile all sources added until now and return {@link CompilationResult},
	 * providing access to the compiled classes and/or errors and warnings
	 */
	public CompilationResult compile() throws Exception {
		if (sourceCodes.size() == 0) {
			throw new CompilationException("No source code to compile");
		}
		Collection<SourceCode> compilationUnits = sourceCodes.values();
		CompiledCode[] code;

		code = new CompiledCode[compilationUnits.size()];
		Iterator<SourceCode> iter = compilationUnits.iterator();
		for (int i = 0; i < code.length; i++) {
			code[i] = new CompiledCode(iter.next().getClassName());
		}
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(
				javac.getStandardFileManager(null, null, null), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null,
				compilationUnits);
		task.call();
		boolean hasWarnings;
		boolean hasErrors;

		{
			boolean hasWarningsTmp = false;
			boolean hasErrorsTmp = false;
			for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
				switch (d.getKind()) {
				case NOTE:
				case MANDATORY_WARNING:
				case WARNING:
					hasWarningsTmp = true;
					break;
				case OTHER:
				case ERROR:
				default:
					hasErrorsTmp = true;
					break;
				}
			}
			hasWarnings = hasWarningsTmp;
			hasErrors = hasErrorsTmp;
		}

		return new CompilationResult() {

			@Override
			public Map<String, Class<?>> classMap() throws ClassNotFoundException {
				Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
				for (String className : sourceCodes.keySet()) {
					classes.put(className, classLoader.loadClass(className));
				}
				return classes;
			}

			@Override
			public boolean compilationSucceeded() {
				if (hasWarnings && !ignoreWarnings)
					return false;
				return !hasErrors;
			}

			@Override
			public boolean hasWarnings() {
				return hasWarnings;
			}

			@Override
			public boolean hasErrors() {
				return hasErrors;
			}

			@Override
			public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
				return collector.getDiagnostics();
			}

			@Override
			public CompilationResult checkNoErrors() {
				if (!compilationSucceeded()) {
					StringBuffer exceptionMsg = new StringBuffer();
					exceptionMsg.append("Unable to compile the source");
					for (Diagnostic<? extends JavaFileObject> d : getDiagnostics()) {
						exceptionMsg.append("\n").append("[kind=").append(d.getKind());
						exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
						exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
					}
					throw new CompilationException(exceptionMsg.toString());
				}
				return this;
			}

		};
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
