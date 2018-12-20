// copied and adapted from ANTLR 4 Java runtime
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
package org.antlr.intellij.adaptor.xpath;

import com.intellij.lang.Language;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.antlr.intellij.adaptor.psi.Trees;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.xpath.XPathLexer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Represent a subset of XPath XML path syntax for use in identifying nodes in
 * parse trees.
 *
 * <p>
 * Split path into words and separators {@code /} and {@code //} via ANTLR
 * itself then walk path elements from left to right. At each separator-word
 * pair, find set of nodes. Next stage uses those as work list.</p>
 *
 * <p>
 * The basic interface is
 * {@link XPath#findAll}{@code (tree, pathString, parser)}.
 * But that is just shorthand for:</p>
 *
 * <pre>
 * {@link XPath} p = new {@link XPath#XPath XPath}(parser, pathString);
 * return p.{@link #evaluate evaluate}(tree);
 * </pre>
 *
 * <p>
 * See {@code org.antlr.v4.test.TestXPath} for descriptions. In short, this
 * allows operators:</p>
 *
 * <dl>
 * <dt>/</dt> <dd>root</dd>
 * <dt>//</dt> <dd>anywhere</dd>
 * <dt>!</dt> <dd>invert; this must appear directly after root or anywhere
 * operator</dd>
 * </dl>
 *
 * <p>
 * and path elements:</p>
 *
 * <dl>
 * <dt>ID</dt> <dd>token name</dd>
 * <dt>'string'</dt> <dd>any string literal token from the grammar</dd>
 * <dt>expr</dt> <dd>rule name</dd>
 * <dt>*</dt> <dd>wildcard matching any node</dd>
 * </dl>
 *
 * <p>
 * Whitespace is not allowed.</p>
 */
public class XPath {
	public static final String WILDCARD = "*"; // word not operator/separator
	public static final String NOT = "!"; 	   // word for invert operator

	private final List<TokenIElementType> tokenElementTypes;
	private final List<RuleIElementType> ruleElementTypes;
	private final Map<String, Integer> 	 ruleIndexes;
	private final Map<String, Integer> 	 tokenTypes;

	protected String path;

	public XPath(Language language, String path) {
		this.path = path;
		this.tokenElementTypes = PSIElementTypeFactory.getTokenIElementTypes(language);
		this.ruleElementTypes  = PSIElementTypeFactory.getRuleIElementTypes(language);
		this.ruleIndexes       = PSIElementTypeFactory.getRuleNameToIndexMap(language);
		this.tokenTypes        = PSIElementTypeFactory.getTokenNameToTypeMap(language);
	}

	// TODO: check for invalid token/rule names, bad syntax

	public XPathElement[] split(String path) {
		ANTLRInputStream in;
		try {
			in = new ANTLRInputStream(new StringReader(path));
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("Could not read path: "+path, ioe);
		}
		XPathLexer lexer = new XPathLexer(in) {
			public void recover(LexerNoViableAltException e) { throw e;	}
		};
		lexer.removeErrorListeners();
		lexer.addErrorListener(new XPathLexerErrorListener());
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		try {
			tokenStream.fill();
		}
		catch (LexerNoViableAltException e) {
			int pos = lexer.getCharPositionInLine();
			String msg = "Invalid tokens or characters at index "+pos+" in path '"+path+"'";
			throw new IllegalArgumentException(msg, e);
		}

		List<Token> tokens = tokenStream.getTokens();
//		System.out.println("path="+path+"=>"+tokens);
		List<XPathElement> elements = new ArrayList<XPathElement>();
		int n = tokens.size();
		int i=0;
loop:
		while ( i<n ) {
			Token el = tokens.get(i);
			Token next = null;
			switch ( el.getType() ) {
				case XPathLexer.ROOT :
				case XPathLexer.ANYWHERE :
					boolean anywhere = el.getType() == XPathLexer.ANYWHERE;
					i++;
					next = tokens.get(i);
					boolean invert = next.getType()==XPathLexer.BANG;
					if ( invert ) {
						i++;
						next = tokens.get(i);
					}
					XPathElement pathElement = getXPathElement(next, anywhere);
					pathElement.invert = invert;
					elements.add(pathElement);
					i++;
					break;

				case XPathLexer.TOKEN_REF :
				case XPathLexer.RULE_REF :
				case XPathLexer.WILDCARD :
					elements.add( getXPathElement(el, false) );
					i++;
					break;

				case Token.EOF :
					break loop;

				default :
					throw new IllegalArgumentException("Unknowth path element "+el);
			}
		}
		return elements.toArray(new XPathElement[0]);
	}

	/**
	 * Convert word like {@code *} or {@code ID} or {@code expr} to a path
	 * element. {@code anywhere} is {@code true} if {@code //} precedes the
	 * word.
	 */
	protected XPathElement getXPathElement(Token wordToken, boolean anywhere) {
		if ( wordToken.getType()==Token.EOF ) {
			throw new IllegalArgumentException("Missing path element at end of path");
		}
		String word = wordToken.getText();
		Integer ttype = tokenTypes.get(word);
		Integer ruleIndex = ruleIndexes.get(word);
		switch ( wordToken.getType() ) {
			case XPathLexer.WILDCARD :
				return anywhere ?
					new XPathWildcardAnywhereElement() :
					new XPathWildcardElement();
			case XPathLexer.TOKEN_REF :
			case XPathLexer.STRING :
				if ( ttype==null || ttype==Token.INVALID_TYPE ) {
					throw new IllegalArgumentException(word+
													   " at index "+
													   wordToken.getStartIndex()+
													   " isn't a valid token name");
				}
				return anywhere ?
					new XPathTokenAnywhereElement(word, ttype) :
					new XPathTokenElement(word, ttype);
			default :
				if ( ruleIndex==null || ruleIndex==-1 ) {
					throw new IllegalArgumentException(word+
													   " at index "+
													   wordToken.getStartIndex()+
													   " isn't a valid rule name");
				}
				return anywhere ?
					new XPathRuleAnywhereElement(word, ruleIndex) :
					new XPathRuleElement(word, ruleIndex);
		}
	}


	public static Collection<? extends PsiElement> findAll(Language language, PsiElement tree, String xpath) {
		XPath p = new XPath(language, xpath);
		XPathElement[] elements = p.split(xpath);
		return p.evaluate(tree, elements);
	}

	public static class DummyRoot extends CompositePsiElement {
		public final PsiElement child;
		public DummyRoot(PsiElement child) {
			super(GeneratedParserUtilBase.DUMMY_BLOCK);
			this.child = child;
		}

		@NotNull
		@Override
		public PsiElement[] getChildren() {
			return new PsiElement[] {child};
		}

		@NotNull
		@Override
		public PsiReference[] getReferences() {
			return PsiReference.EMPTY_ARRAY;
		}

		@NotNull
		@Override
		public Language getLanguage() {
			return getParent().getLanguage();
		}
	}

	/**
	 * Return a list of all nodes starting at {@code t} as root that satisfy the
	 * path. The root {@code /} is relative to the node passed to
	 * {@link #evaluate}.
	 */
	public Collection<? extends PsiElement> evaluate(PsiElement t, XPathElement[] elements) {
		if ( t==null ) return Collections.emptyList();

		if ( t instanceof PsiFile ) {
			// the PSI fileroot exists above start rule in ANTLR grammar and hence above ANTLR's parse tree root
			// drop t down to top of ANTLR's tree. Should be only child if we ignore WS, Comments
			t = Trees.getChildren(t)[0];
		}
		PsiElement dummyRoot = new DummyRoot(t); // a dummy parent of t so we can initialize the work list

		Collection<PsiElement> work = Collections.singleton(dummyRoot);

		int i = 0;
		while ( i < elements.length ) {
			Collection<PsiElement> next = new LinkedHashSet<>();
			for (PsiElement node : work) {
				if ( node.getChildren().length>0 ) {
					// only try to match next element if it has children
					// e.g., //func/*/stat might have a token node for which
					// we can't go looking for stat nodes.
					Collection<? extends PsiElement> matching = elements[i].evaluate(node);
					next.addAll(matching);
				}
			}
			i++;
			work = next;
		}

		return work;
	}
}
