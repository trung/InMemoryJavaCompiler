package org.mdkt.compiler;

import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryJavaCompilerTest {
	private static final Logger logger = LoggerFactory.getLogger(InMemoryJavaCompilerTest.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public String getResourceAsString(String path) throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream srcResStream = classloader.getResourceAsStream(path);

		// Thanks to https://www.baeldung.com/convert-input-stream-to-string
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte[] chunk = new byte[4096];
		int bytesRead = 0;
		while ((bytesRead = srcResStream.read(chunk, 0, chunk.length)) != -1) {
			buf.write(chunk, 0, bytesRead);
		}
		return new String(buf.toByteArray(), StandardCharsets.UTF_8);
	}

	@Test
	public void compile_WhenTypical() throws Exception {
		Class<?> helloClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenTypical/HelloClass.java"));
		Assert.assertNotNull(helloClass);
		Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
	}

	@Test
	public void compileAll_WhenTypical() throws Exception {
		Map<String, Class<?>> compiled = InMemoryJavaCompiler.newInstance().addSource("A", getResourceAsString("compileAll_WhenTypical/A.java")).addSource("B", getResourceAsString("compileAll_WhenTypical/B.java")).compileAll();

		Assert.assertNotNull(compiled.get("A"));
		Assert.assertNotNull(compiled.get("B"));

		Class<?> aClass = compiled.get("A");
		Object a = aClass.newInstance();
		Assert.assertEquals("B!", aClass.getMethod("b").invoke(a).toString());
	}

	@Test
	public void compile_WhenSourceContainsInnerClasses() throws Exception {
		Class<?> helloClass = InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenSourceContainsInnerClasses/HelloClass.java"));
		Assert.assertNotNull(helloClass);
		Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
	}

	@Test
	public void compile_whenError() throws Exception {
		thrown.expect(CompilationException.class);
		thrown.expectMessage("Unable to compile the source");
		InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", getResourceAsString("compile_whenError/HelloClass.java"));
	}

	@Test
	public void compile_WhenFailOnWarnings() throws Exception {
		thrown.expect(CompilationException.class);
		InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenFailOnWarnings/HelloClass.java"));
	}

	@Test
	public void compile_WhenIgnoreWarnings() throws Exception {
		Class<?> helloClass = InMemoryJavaCompiler.newInstance().ignoreWarnings().compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenIgnoreWarnings/HelloClass.java"));
		List<?> res = (List<?>) helloClass.getMethod("hello").invoke(helloClass.newInstance());
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void compile_WhenWarningsAndErrors() throws Exception {
		thrown.expect(CompilationException.class);
		try {
			InMemoryJavaCompiler.newInstance().compile("org.mdkt.HelloClass", getResourceAsString("compile_WhenWarningsAndErrors/HelloClass.java"));
		} catch (Exception e) {
			logger.info("Exception caught: {}", e);
			throw e;
		}
	}
}
