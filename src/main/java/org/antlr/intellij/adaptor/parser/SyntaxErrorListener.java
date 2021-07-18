package org.antlr.intellij.adaptor.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Utils;

import java.util.ArrayList;
import java.util.List;

/** Traps errors from parsing language of plugin. E.g., for a Java plugin,
 *  this would catch errors when people type invalid Java code into .java file.
 *  This swallows the errors as the PSI tree has error nodes.
 */
public class SyntaxErrorListener extends BaseErrorListener {
	private final List<SyntaxError> syntaxErrors = new ArrayList<>();

	public SyntaxErrorListener() {
	}

	public List<SyntaxError> getSyntaxErrors() {
		return syntaxErrors;
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
							Object offendingSymbol,
							int line, int charPositionInLine,
							String msg, RecognitionException e)
	{
		syntaxErrors.add(new SyntaxError(recognizer, (Token)offendingSymbol, line, charPositionInLine, msg, e));
	}

	@Override
	public String toString() {
		return Utils.join(syntaxErrors.iterator(), "\n");
	}
}
