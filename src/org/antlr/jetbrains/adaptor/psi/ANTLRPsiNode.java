package org.antlr.jetbrains.adaptor.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

/** This class represents an internal, non-leaf "composite" PSI
 *  node. The term "ANTLRPsiNode" is somewhat of a misnomer as it's
 *  really just a PSI node but make sure that getChildren() acts as I
 *  would expect, returning all children.
 *
 *  The IElementType of the associated ASTNode's will be {@link
 *  RuleIElementType} and the ASTNode's associated with LeafElement's
 *  are {@link TokenIElementType}. The only exception is when parsing
 *  snippets of code. In that case, Intellij calls
 *  ParserDefinition.createElement() with a TokenIElementType (usually
 *  identifier) as the root of the generated PSI tree. So, when
 *  looking for an ID, you might get a tree like (ID (expr (primary
 *  ID))). Weird but whatever. The root ID is an ANTLRPsiNode and the
 *  leaf ID is an ANTLRPsiLeafNode.
 *
 *  I've come to the conclusion that it's much easier to build plug-ins
 *  when you have PSI trees with heterogeneous types. I typically make
 *  subclasses of ANTLRPsiNode for each kind of declaration for use with
 *  renaming, find usages, and jump to declaration.
 */
public class ANTLRPsiNode extends ASTWrapperPsiElement {
	public ANTLRPsiNode(@NotNull ASTNode node) {
		super(node);
	}

	/** For some reason, default impl of this only returns rule refs
	 *  (composite nodes in jetbrains speak) but we want ALL children.
	 *  Well, we don't want hidden channel stuff.
	 */
	@Override
	@NotNull
	public PsiElement[] getChildren() { return Trees.getChildren(this); }

	/** For this internal PSI node, look upward for our enclosing scope.
	 *  Start looking for a scope at our parent node so getContext()
	 *  returns the enclosing scope (context) when this is a ScopeNode.
	 *
	 *  From the return to scope node, you typically look for a declaration
	 *  by looking at its children.
	 */
	@Override
	public ScopeNode getContext() {
		PsiElement parent = getParent();
		if ( parent instanceof ScopeNode ) {
			return (ScopeNode)parent;
		}
		return (ScopeNode)parent.getContext();
//		while ( p!=null && !(p instanceof ScopeNode) ) {
//			p = p.getParent();
//		}
//		return (ScopeNode)p;
	}
}
