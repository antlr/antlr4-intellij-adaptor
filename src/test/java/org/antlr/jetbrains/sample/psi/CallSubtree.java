package org.antlr.jetbrains.sample.psi;

import com.intellij.lang.ASTNode;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

public class CallSubtree extends ANTLRPsiNode {
	public CallSubtree(@NotNull ASTNode node) {
		super(node);
	}
}
