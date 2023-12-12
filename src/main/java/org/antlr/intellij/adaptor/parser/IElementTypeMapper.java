package org.antlr.intellij.adaptor.parser;

import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

/**
 * Maps a {@link org.antlr.v4.runtime.ParserRuleContext} to an {@link com.intellij.psi.tree.IElementType}.
 */
public interface IElementTypeMapper {
	IElementType toIElementType(ParserRuleContext ctx);

	List<RuleIElementType> getRuleElementTypes();
}
