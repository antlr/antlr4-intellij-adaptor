package org.antlr.intellij.adaptor.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.antlr.intellij.adaptor.SymtabUtils;

/** A leaf node you can use as a superclass for your PSI trees.
 *  You don't have to use it of course, but it gives you basic
 *  simple scoping behavior via getContext().
 *
 *  I recommends creating a subclass for identifiers, such as
 *  MyLanguageIDNode. To enable rename, find usages, etc... that
 *  node will need to implement PsiNamedElement.
 */
public class ANTLRPsiLeafNode extends LeafPsiElement {
	public ANTLRPsiLeafNode(IElementType type, CharSequence text) {
		super(type, text);
	}

	/** We're a leaf node so must start looking at parent node for a scope.
	 *  This assumes a reasonable getContext() implementation for your
	 *  internal, non-leaf PSI nodes. It's easiest to use {@link ANTLRPsiNode}
	 *  subclasses for your internal notes.
	 */
	@Override
	public PsiElement getContext() {
		return SymtabUtils.getContextFor(this);
	}
}
