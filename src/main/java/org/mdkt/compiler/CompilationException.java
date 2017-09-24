package org.mdkt.compiler;

import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompilationException extends RuntimeException {
	private static final long serialVersionUID = 5272588827551900536L;

	public CompilationException(List<Diagnostic<? extends JavaFileObject>> diags) {
		super(buildMessage(diags));
	}

	private static String buildMessage(List<Diagnostic<? extends JavaFileObject>> diags) {
		StringBuffer msg = new StringBuffer();
		msg.append("Unable to compile the source.");
		for (Diagnostic<?> diag : diags) {
			msg.append("\n").append("[kind=").append(diag.getKind());
			msg.append(", ").append("line=").append(diag.getLineNumber());
			msg.append(", ").append("message=").append(diag.getMessage(Locale.US)).append("]");
		}
		return msg.toString();
	}
}
