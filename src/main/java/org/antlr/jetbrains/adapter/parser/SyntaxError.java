package org.antlr.jetbrains.adapter.parser;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/** A syntax error from parsing language of plugin. These are
 *  created by SyntaxErrorListener.
 */
public class SyntaxError {
	private final Recognizer<?, ?> recognizer;
	private final Token offendingSymbol;
	private final int line;
	private final int charPositionInLine;
	private final String message;
	private final RecognitionException e;

	public SyntaxError(Recognizer<?, ?> recognizer,
	                   Token offendingSymbol,
	                   int line, int charPositionInLine,
	                   String msg,
	                   RecognitionException e)
	{
		this.recognizer = recognizer;
		this.offendingSymbol = offendingSymbol;
		this.line = line;
		this.charPositionInLine = charPositionInLine;
		this.message = msg;
		this.e = e;
	}

	public Recognizer<?, ?> getRecognizer() {
		return recognizer;
	}

	public Token getOffendingSymbol() {
		if ( e instanceof NoViableAltException ) {
			// the error node in parse tree will have the start token as bad token
			// even if many lookahead tokens were matched before failing to find
			// a viable alt.
			return ((NoViableAltException) e).getStartToken();
		}
		return offendingSymbol;
	}

	public int getLine() {
		return line;
	}

	public int getCharPositionInLine() {
		return charPositionInLine;
	}

	public String getMessage() {
		return message;
	}

	public RecognitionException getException() {
		return e;
	}
}
