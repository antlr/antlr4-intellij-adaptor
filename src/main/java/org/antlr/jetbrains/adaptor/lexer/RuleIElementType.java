package org.antlr.jetbrains.adaptor.lexer;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Represents a specific ANTLR rule invocation in the language of the plug-in and is the
 *  intellij "token type" of an interior PSI tree node. The IntelliJ equivalent
 *  of ANTLR RuleNode.getRuleIndex() method or maybe RuleNode itself.
 *
 *  Intellij Lexer token types are instances of IElementType.
 *  We differentiate between parse tree subtree roots and tokens with
 *  {@link RuleIElementType} and {@link TokenIElementType}.
 */
public class RuleIElementType extends IElementType {
	private final int ruleIndex;

	public RuleIElementType(int ruleIndex,
	                        @NotNull @NonNls String debugName,
	                        @Nullable Language language)
	{
		super(debugName, language);
		this.ruleIndex = ruleIndex;
	}

	public int getRuleIndex() {
		return ruleIndex;
	}
}
