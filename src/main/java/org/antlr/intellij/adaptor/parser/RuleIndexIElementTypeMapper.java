package org.antlr.intellij.adaptor.parser;

import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

/**
 * Maps a {@link org.antlr.v4.runtime.ParserRuleContext} to an {@link com.intellij.psi.tree.IElementType} using
 * a rule index.
 */
public class RuleIndexIElementTypeMapper implements IElementTypeMapper {

	private final List<RuleIElementType> ruleElementTypes;

	public RuleIndexIElementTypeMapper(List<RuleIElementType> elementTypes) {
		this.ruleElementTypes = elementTypes;
	}

	@Override
	public IElementType toIElementType(ParserRuleContext ctx) {
		return ruleElementTypes.get(ctx.getRuleIndex());
	}

	@Override
	public List<RuleIElementType> getRuleElementTypes() {
		return ruleElementTypes;
	}
}
