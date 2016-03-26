package org.antlr.jetbrains.adapter.xpath;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.testFramework.ParsingTestCase;

import java.io.IOException;
import java.util.Collection;


import org.antlr.jetbrains.sample.SampleLanguage;
import org.antlr.jetbrains.sample.SampleParserDefinition;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class XPathTest extends ParsingTestCase {

    public XPathTest() {
        super("", "Sample", new SampleParserDefinition());
    }

    public void testSingleVarDef() throws Exception {
        String code = "var x = 1";
        String output = code;
        String xpath = "/script/vardef";
        checkXPathResults(code, xpath, output);
    }

    public void testMultiVarDef() throws Exception {
        String code =
                "var x = 1\n" +
                        "var y = [1,2,3]\n";
        String output = code;
        String xpath = "/script/vardef";
        checkXPathResults(code, xpath, output);
    }

    public void testFuncNames() throws Exception {
        String code = loadFile("src/test/resources/test.sample");
        String output =
                "f\n"+
                        "g\n"+
                        "h";
        String xpath = "/script/function/ID";
        checkXPathResults(code, xpath, output);
    }

    public void testAllIDs() throws Exception {
        String code = loadFile("src/test/resources/test.sample");
        String output =
                "f\n"+
                        "x\n"+
                        "y\n"+
                        "x\n"+
                        "x\n"+
                        "g\n"+
                        "x\n"+
                        "y\n"+
                        "h\n" +
                        "z";
        String xpath = "//ID";
        checkXPathResults(code, xpath, output);
    }

    public void testAnyVarDef() throws Exception {
        String code = loadFile("src/test/resources/test.sample");
        String output =
                "var y = x\n"+
                        "var z = 9";
        String xpath = "//vardef";
        checkXPathResults(code, xpath, output);
    }

    public void testVarDefIDs() throws Exception {
        String code = loadFile("src/test/resources/test.sample");
        String output =
                "y\n" +
                        "z";
        String xpath = "//vardef/ID";
        checkXPathResults(code, xpath, output);
    }

    public void testAllVarDefIDsInScopes() throws Exception {
        String code = loadFile("src/test/resources/bubblesort.sample");
        String output =
                "x\n"+
                        "i\n"+
                        "j\n"+
                        "swap\n"+
                        "x";
        String xpath = "//block/vardef/ID";
        checkXPathResults(code, xpath, output);
    }

    public void testTopLevelVarDefIDsInScopes() throws Exception {
        String code = loadFile("src/test/resources/bubblesort.sample");
        String output =
                "x\n"+
                        "i";
        String xpath = "//function/block/vardef/ID";
        checkXPathResults(code, xpath, output);
    }

    public void testRuleUnderWildcard() throws Exception {
        String code = loadFile("src/test/resources/test.sample");
        String output =
                        "x\n"+
                        "[\n"+
                        "1\n"+
                        "]\n"+
                        "=\n"+
                        "\"sdflkjsdf\"\n"+
                        "return\n"+
                        "false;";
        String xpath = "//function/*/statement/*";
        checkXPathResults(code, xpath, output);
    }

    public void testAllNonWhileTokens() throws Exception {
        String code = loadFile("src/test/resources/bubblesort.sample");
        String output =
                "(\n"+
                        ")\n"+
                        "return";
        String xpath = "/script/function/block/statement/!'while'";
        checkXPathResults(code, xpath, output);
    }

    public void testGetNestedIf() throws Exception {
        String code = loadFile("src/test/resources/bubblesort.sample");
        String output =
                "if";
        String xpath = "//'if'";
        checkXPathResults(code, xpath, output);
    }

    public void testWildcardUnderFuncThenJustTokens() throws Exception {
        String code = loadFile("src/test/resources/test.sample");
        String output =
                "func\n"+
                        "f\n"+
                        "(\n"+
                        ")\n"+
                        "func\n"+
                        "g\n"+
                        "(\n"+
                        ")\n"+
                        "func\n"+
                        "h\n"+
                        "(\n"+
                        ")\n"+
                        ":";
        String xpath = "//function/*";
        myFile = createPsiFile("a", code);
        ensureParsed(myFile);
        assertEquals(code, myFile.getText());
        final StringBuilder buf = new StringBuilder();
        Collection<? extends PsiElement> nodes = XPath.findAll(SampleLanguage.INSTANCE, myFile, xpath);
        for (PsiElement n : nodes) {
            if ( n instanceof LeafPsiElement) {
                buf.append(n.getText());
                buf.append("\n");
            }
        }
        assertEquals(output.trim(), buf.toString().trim());
    }

    // S U P P O R T

    protected void checkXPathResults(String code, String xpath, String allNodesText) throws IOException {
        checkXPathResults(SampleLanguage.INSTANCE, code, xpath, allNodesText);
    }

    protected void checkXPathResults(Language language, String code, String xpath, String allNodesText) throws IOException {
        myFile = createPsiFile("a", code);
        ensureParsed(myFile);
        assertEquals(code, myFile.getText());
        Collection<? extends PsiElement> nodes = XPath.findAll(language, myFile, xpath);
        StringBuilder buf = new StringBuilder();
        for (PsiElement t : nodes) {
            buf.append(t.getText());
            buf.append("\n");
        }
        assertEquals(allNodesText.trim(), buf.toString().trim());
    }

    @Override
    protected String getTestDataPath() {
        return ".";
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }
}
