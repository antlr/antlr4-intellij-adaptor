package org.antlr.jetbrains.sample.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class ArgdefSubtree extends VardefSubtree {
	public ArgdefSubtree(@NotNull ASTNode node) {
		super(node);
	}
}
