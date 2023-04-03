package org.antlr.intellij.adaptor.parser;

import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassNameIElementTypeMapper implements IElementTypeMapper {

	private final Map<String, RuleIElementType> elementTypes;

	public ClassNameIElementTypeMapper(List<RuleIElementType> elementTypes) {
		this.elementTypes = elementTypes.stream()
			.collect(Collectors.toMap(RuleIElementType::getDebugName, el -> el));
	}

	@Override
	public IElementType toIElementType(ParserRuleContext ctx) {
		String elementTypeName = StringUtils.removeEnd(ctx.getClass().getSimpleName(), "Context");
		return elementTypes.get(elementTypeName);
	}

	@Override
	public List<RuleIElementType> getRuleElementTypes() {
		return new ArrayList<>(elementTypes.values());
	}
}
