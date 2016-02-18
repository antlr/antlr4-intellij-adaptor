package org.antlr.jetbrains.adaptor.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The superclass of nodes in your PSI tree that come from ANTLR subtree
 *  roots and that represent phrases that define/declare variables, functions, ...
 *  Typical subclasses include FunctionSubtree, VardefSubtree, ...
 *  To integrate such subclasses, this class implements PsiNameIdentifierOwner.
 *
 *  PsiNameIdentifierOwner points at the root of an entire definition statement
 *  rather than the individual ID node within that subtree. I then have
 *  getNameIdentifier() return that ID child. See
 *  <a href="https://devnet.jetbrains.com/message/5566711#5566711">forum thread</a>
 *  for more details.
 */
public abstract class IdentifierDefSubtree extends ANTLRPsiNode implements PsiNameIdentifierOwner {
	/**
	 * Gets the {@link IElementType} of the ID/Identifier rule for
	 * use in {@link #getNameIdentifier}
	 */
	private final IElementType idElementType;

	public IdentifierDefSubtree(@NotNull ASTNode node, @NotNull IElementType idElementType) {
		super(node);
		this.idElementType = idElementType;
	}

	@Override
	public String getName() {
		PsiElement id = getNameIdentifier();
		return id!=null ? id.getText() : null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier() {
		ASTNode idNode = getNode().findChildByType(idElementType);
		if (idNode != null) {
			return idNode.getPsi();
		}
		return null;
	}

	/** This method indicates where the ID node associated with this def subtree
	 *  starts relative to the start position of the text of this subtree.
	 */
	@Override
	public int getTextOffset() {
		PsiElement id = getNameIdentifier();
		return id!=null ? id.getTextOffset() : super.getTextOffset();
	}

	/** Delegate responsibility for setting the name of a definition subtree
	 *  to the actual ID descendant (usually and immediate child), which
	 *  willReplace that node as necessary.
	 */
	@Override
	public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
//		System.out.println(getClass().getSimpleName()+".setName("+name+") on "+
//			                   this+" at "+Integer.toHexString(this.hashCode()));
		PsiNamedElement idNode = (PsiNamedElement)getNameIdentifier();
		if ( idNode!=null ) {
			return idNode.setName(name);
		}
		return this;
	}
}
