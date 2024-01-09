package issue2;

import com.intellij.testFramework.ParsingTestCase;

public class Issue2ParserTest extends ParsingTestCase {

	public Issue2ParserTest() {
		super("issue2", "ext", true, new Issue2ParserDefinition());
	}

	public void testIssue2() {
		doTest(true);
	}

	@Override
	protected String getTestDataPath() {
		return "src/test/resources/testData";
	}
}
