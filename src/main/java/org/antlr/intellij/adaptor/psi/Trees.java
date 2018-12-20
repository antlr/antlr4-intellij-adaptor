// copied from ANTLR 4 Java runtime
/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.antlr.intellij.adaptor.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trees {
	public interface Predicate<T> {
		boolean test(T t);
	}

	private Trees() { }

	/** Return a list of all ancestors of this node.  The first node of
	 *  list is the root and the last is the parent of this node.
	 */
	public static List<? extends PsiElement> getAncestors(PsiElement t) {
		if ( t.getParent()==null ) return Collections.emptyList();
		List<PsiElement> ancestors = new ArrayList<>();
		t = t.getParent();
		while ( t!=null ) {
			ancestors.add(0, t); // insert at start
			t = t.getParent();
		}
		return ancestors;
	}

	/** Return true if t is u's parent or a node on path to root from u.
	 *  Use == not equals().
	 */
	public static boolean isAncestorOf(PsiElement t, PsiElement u) {
		if ( t==null || u==null || t.getParent()==null ) return false;
		PsiElement p = u.getParent();
		while ( p!=null ) {
			if ( t==p ) return true;
			p = p.getParent();
		}
		return false;
	}

	public static ANTLRPsiNode getRoot(PsiElement t) {
		PsiFile contextOfType = PsiTreeUtil.getParentOfType(t, PsiFile.class);
		return (ANTLRPsiNode)Trees.getChildren(contextOfType)[0];
	}

	/** From collection of nodes, make a map from the text of the node to the
	 *  node.
	 */
	public static Map<String, PsiElement> toMap(Collection<? extends PsiElement> nodes) {
		HashMap<String,PsiElement> m = new HashMap<>();

		for (PsiElement node : nodes) {
			m.put(node.getText(), node);
		}
		return m;
	}

	public static Collection<PsiElement> findAllTokenNodes(PsiElement t, int ttype) {
		return findAllNodes(t, ttype, true);
	}

	public static Collection<PsiElement> findAllRuleNodes(PsiElement t, int ruleIndex) {
		return findAllNodes(t, ruleIndex, false);
	}

	public static List<PsiElement> findAllNodes(PsiElement t, int index, boolean findTokens) {
		List<PsiElement> nodes = new ArrayList<>();
		_findAllNodes(t, index, findTokens, nodes);
		return nodes;
	}

	public static void _findAllNodes(PsiElement t, int index, boolean findTokens,
									 List<? super PsiElement> nodes)
	{
		// check this node (the root) first
		if ( findTokens && t instanceof LeafPsiElement ) {
			LeafPsiElement tnode = (LeafPsiElement)t;
			IElementType elType = tnode.getNode().getElementType();
			if ( elType instanceof TokenIElementType) {
				if ( ((TokenIElementType) elType).getANTLRTokenType()==index ) {
					nodes.add(t);
				}
			}
		}
		else if ( !findTokens && t instanceof ANTLRPsiNode ) {
			ANTLRPsiNode ctx = (ANTLRPsiNode)t;
			IElementType elType = ctx.getNode().getElementType();
			if ( elType instanceof RuleIElementType) {
				if ( ((RuleIElementType) elType).getRuleIndex()==index ) nodes.add(t);
			}
		}
		// check children
		for (PsiElement c : t.getChildren()) {
			_findAllNodes(c, index, findTokens, nodes);
		}
	}

	/** Get all descendents; includes t itself. */
	public static List<PsiElement> getDescendants(PsiElement t) {
		List<PsiElement> nodes = new ArrayList<>();
		nodes.add(t);

		for (PsiElement c : t.getChildren()) {
			nodes.addAll(getDescendants(c));
		}
		return nodes;
	}

	/** Get all non-WS, non-Comment children of t */
	@NotNull
	public static PsiElement[] getChildren(PsiElement t) {
		if ( t==null ) return PsiElement.EMPTY_ARRAY;

		PsiElement psiChild = t.getFirstChild();
		if (psiChild == null) return PsiElement.EMPTY_ARRAY;

		List<PsiElement> result = new ArrayList<>();
		while (psiChild != null) {
			if ( !(psiChild instanceof PsiComment) &&
				 !(psiChild instanceof PsiWhiteSpace) )
			{
				result.add(psiChild);
			}
			psiChild = psiChild.getNextSibling();
		}
		return PsiUtilCore.toPsiElementArray(result);
	}


	/** Find smallest subtree of t enclosing range startCharIndex..stopCharIndex
	 *  inclusively using postorder traversal.  Recursive depth-first-search.
	 */
	public static PsiElement getRootOfSubtreeEnclosingRegion(PsiElement t,
	                                                         int startCharIndex, // inclusive
	                                                         int stopCharIndex)  // inclusive
	{
		for (PsiElement c : t.getChildren()) {
			PsiElement sub = getRootOfSubtreeEnclosingRegion(c, startCharIndex, stopCharIndex);
			if ( sub!=null ) return sub;
		}
		IElementType elementType = t.getNode().getElementType();
		if ( elementType instanceof RuleIElementType ) {
			TextRange r = t.getNode().getTextRange();
			// is range fully contained in t?  Note: jetbrains uses exclusive right end (use < not <=)
			if ( startCharIndex>=r.getStartOffset() && stopCharIndex<r.getEndOffset() ) {
				return t;
			}
		}
		return null;
	}

	/** Return first node satisfying the pred among descendants. Depth-first order. Test includes t itself. */
	public static PsiElement findNodeSuchThat(PsiElement t, Predicate<PsiElement> pred) {
		if ( pred.test(t) ) return t;

		for (PsiElement c : t.getChildren()) {
			PsiElement u = findNodeSuchThat(c, pred);
			if ( u!=null ) return u;
		}
		return null;
	}

	public static PsiElement createLeafFromText(Project project, Language language, PsiElement context,
												String text, IElementType type)
	{
		PsiFileFactoryImpl factory = (PsiFileFactoryImpl) PsiFileFactory.getInstance(project);
		PsiElement el = factory.createElementFromText(text, language, type, context);
		if ( el==null ) return null;
		return PsiTreeUtil.getDeepestFirst(el); // forces parsing of file!!
		// start rule depends on root passed in
	}

	public static void replacePsiFileFromText(final Project project, Language language, final PsiFile psiFile, String text) {
		final PsiFile newPsiFile = createFile(project, language, text);
		if ( newPsiFile==null ) return;
		WriteCommandAction setTextAction = new WriteCommandAction(project) {
			@Override
			protected void run(@NotNull Result result) throws Throwable {
				psiFile.deleteChildRange(psiFile.getFirstChild(), psiFile.getLastChild());
				psiFile.addRange(newPsiFile.getFirstChild(), newPsiFile.getLastChild());
			}
		};
		setTextAction.execute();
	}

	public static PsiFile createFile(Project project, Language language, String text) {
		LanguageFileType ftype = language.getAssociatedFileType();
		if ( ftype==null ) return null;
		String ext = ftype.getDefaultExtension();
		String fileName = "___fubar___."+ext; // random name but must have correct extension
		PsiFileFactoryImpl factory = (PsiFileFactoryImpl)PsiFileFactory.getInstance(project);
		return factory.createFileFromText(fileName, language, text, false, false);
	}
}
