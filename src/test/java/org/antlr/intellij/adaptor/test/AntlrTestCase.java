package org.antlr.intellij.adaptor.test;

import com.intellij.lang.Language;
import com.intellij.testFramework.ParsingTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class AntlrTestCase extends ParsingTestCase{
	
	private final String name;
	
	@Parameterized.Parameters(name = "{index}: {2} for {0}/{1}")
	public static Iterable<Object> parameters(){
		return Arrays.asList((Object[]) new Object[][]{
				{ "Issue2", "block", "issue2" }
		});
	}
	
	public AntlrTestCase(String lang, String root, String name){
		this(lang, root, name, TestLanguage.synthesizeTestLanguage(lang));
	}
	
	private AntlrTestCase(String lang, String root, String name, Language language){
		super(lang, lang, TestParserDefinition.byName(lang, root, language, new TestFileType(lang, language)));
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	protected String getTestDataPath(){
		return "src/test/resources/testData";
	}
	
	@Test
	public void test(){
		doTest(true);
	}
}