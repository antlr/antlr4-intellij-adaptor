package org.antlr.jetbrains.adaptor.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nullable;

/** This interface acts as a tag so that we can identify nodes in
 *  the PSI tree that represent symbol scopes. For example, in
 *  a simple language like C with globals, functions, arguments,
 *  and local blocks you could create a PSI tree with heterogeneous node
 *  types such as FileSubtree, FunctionSubtree, BlockSubtree, etc...
 *  Each of those should implement this interface.  If you use this
 *  mechanism, then the default getContext() mechanism will work; given
 *  a node, it looks upward in the PSI tree for a node that
 *  implements ScopeNode.
 */
public interface ScopeNode extends PsiElement {
	@Nullable
	PsiElement resolve(PsiNamedElement element);

	@Nullable
	@Override // alter return type
	ScopeNode getContext();
}
