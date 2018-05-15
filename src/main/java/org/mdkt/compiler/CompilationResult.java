package org.mdkt.compiler;

import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public interface CompilationResult {
	/**
	 * Return the compiled classes
	 */
	Map<String, Class<?>> classMap() throws ClassNotFoundException;

	/**
	 * Determine if the compilation did succeed
	 */
	boolean compilationSucceeded();

	/**
	 * Determine if any warnings are present
	 */
	boolean hasWarnings();

	/**
	 * Determine if any errors are present
	 */
	boolean hasErrors();

	/**
	 * Return the diagnostics produced by the compiler
	 */
	List<Diagnostic<? extends JavaFileObject>> getDiagnostics();

	/**
	 * Throw an exception if the compilation did not succeed
	 */
	CompilationResult checkNoErrors();

}
