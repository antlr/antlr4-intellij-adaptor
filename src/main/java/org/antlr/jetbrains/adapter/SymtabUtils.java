package org.antlr.jetbrains.adapter;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiNamedElement;
import org.antlr.jetbrains.adapter.psi.ScopeNode;
import org.antlr.jetbrains.adapter.psi.Trees;
import org.antlr.jetbrains.adapter.xpath.XPath;

import java.util.Collection;

public class SymtabUtils {
	/** Return the root of a def subtree chosen from among the
	 *  matches from xpathToIDNodes that matches namedElement's text.
	 *  Assumption: ID nodes are direct children of def subtree roots.
	 */
	public static PsiElement resolve(ScopeNode scope,
	                                 Language language,
	                                 PsiNamedElement namedElement,
	                                 String xpathToIDNodes)
	{
		Collection<? extends PsiElement> defIDNodes =
			XPath.findAll(language, scope, xpathToIDNodes);
		String id = namedElement.getName();
		PsiElement idNode = Trees.toMap(defIDNodes).get(id); // Find identifier node of variable definition
		if ( idNode!=null ) {
			return idNode.getParent(); // return the def subtree root
		}

		// If not found, ask the enclosing scope/context to resolve.
		// That might lead back to this method, but probably with a
		// different xpathToIDNodes (which is why I don't call this method
		// directly).
		ScopeNode context = scope.getContext();
		if ( context!=null ) {
			return context.resolve(namedElement);
		}
		// must be top scope; no resolution for element
		return null;
	}

	public static ScopeNode getContextFor(PsiElement element) {
		PsiElement parent = element.getParent();
		if ( parent instanceof ScopeNode ) {
			return (ScopeNode)parent;
		}
		if ( parent instanceof PsiErrorElement ) {
			return null;
		}
		return (ScopeNode)parent.getContext();
	}
}
