package org.antlr.jetbrains.adapter.parser;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/** Adapt ANTLR's DefaultErrorStrategy so that we add error nodes
 *  for EOF if reached at start of resync's consumeUntil().
 *  Also set start/stop of missing token to always be the current token,
 *  even if that's EOF.
 */
public class ErrorStrategyAdaptor extends DefaultErrorStrategy {
	@Override
	protected void consumeUntil(Parser recognizer, IntervalSet set) {
		Token o = recognizer.getCurrentToken();
		if ( o.getType()==Token.EOF ) {
			recognizer.getRuleContext().addErrorNode(o);
		}
		super.consumeUntil(recognizer, set);
	}

	/** By default ANTLR makes the start/stop -1/-1 for invalid tokens
	 *  which is reasonable but here we want to highlight the
	 *  current position indicating that is where we lack a token.
	 *  if no input, highlight at position 0.
	 */
	protected Token getMissingSymbol(Parser recognizer) {
		Token missingSymbol = super.getMissingSymbol(recognizer);
		// alter the default missing symbol.
		if ( missingSymbol instanceof CommonToken) {
			int start, stop;
			Token current = recognizer.getCurrentToken();
			start = current.getStartIndex();
			stop = current.getStopIndex();
			((CommonToken) missingSymbol).setStartIndex(start);
			((CommonToken) missingSymbol).setStopIndex(stop);
		}
		return missingSymbol;
	}
}
