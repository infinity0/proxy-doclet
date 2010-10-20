package proxy.adapters;

import proxy.adapters.markup.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

/**
** Static utility methods for markup-related adapters.
**
** @author infinity0
*/
final public class Markup {

	/**
	** Default renderer. This is set from the {@code proxy.adapters.markup}
	** system property, which can take the following values:
	**
	** - pre
	** - pandoc (default)
	** - mediawiki
	**
	** TODO add some more functionality
	*/
	final private static MarkupRenderer renderer = getDefaultRenderer();

	private Markup() { }

	private static MarkupRenderer getDefaultRenderer() {
		String adapter = System.getProperty("proxy.adapters.markup");
		// TODO use reflection

		if (adapter == null) {
			return new PandocMarkdownRenderer(); // default
		} else if (adapter.equals("pre")) {
			return new PreformatRenderer();
		} else if (adapter.equals("pandoc")) {
			return new PandocMarkdownRenderer();
		} else if (adapter.equals("mediawiki")) {
			return new MediaWikiRenderer();
		} else {
			return new PandocMarkdownRenderer(); // fallback
		}
	}

	/**
	** Render the given text using the {@link #renderer default renderer}.
	*/
	public static String render(String text) {
		return renderer.render(dedent(text));
	}

	/**
	** The Standard Java Doclet strips any leading "*" from the comment lines,
	** but it's customary to have spaces after it. This method removes the
	** extraneous spaces so that the renderer adapters don't need to bother
	** with such details.
	**
	** For example:
	**
	** ~~~~
	** /**
	** ** This is a javadoc comment
	** ** that spans several lines.
	** -/
	** ~~~~
	**
	** The Standard Java Doclet processes it into this:
	**
	** ~~~~
	** This is a javadoc comment
	**  that spans several lines.
	** ~~~~
	**
	** This method further converts it to this:
	**
	** ~~~~
	** This is a javadoc comment
	** that spans several lines.
	** ~~~~
	*/
	public static String dedent(String text) {
		try {
			StringReader in = new StringReader(text);
			BufferedReader rd = new BufferedReader(in);
			in.mark(Integer.MAX_VALUE);

			String line = rd.readLine(); // skip first line, Standard Java Doclet has already stripped it
			int indent=Integer.MAX_VALUE, i = 0;

			// guess intended indent from first 4 non-blank lines
			for (line=rd.readLine(); line!=null && i<4; line=rd.readLine(), ++i) {
				int j = getIndent(line);
				if (j == line.length()) { continue; }
				if (j < indent) { indent = j; }
			}

			StringWriter str = new StringWriter(text.length());
			PrintWriter out = new PrintWriter(str);
			rd = new BufferedReader(in);
			in.reset();

			// dedent all lines
			for (line=rd.readLine(); line!=null; line=rd.readLine()) {
				int j = getIndent(line);
				out.println(line.substring(indent > j? j: indent)); // strip the indent (or smaller) from each line
			}
			return str.toString();

		} catch (IOException e) {
			throw new AssertionError();
		}
	}

	/**
	** Return the number of consecutive characters prefixing a line.
	*/
	public static int getIndent(String line, char ic) {
		int j=-1;
		while (++j < line.length() && line.charAt(j) == ic);
		return j;
	}

	/**
	** Return the number of consecutive spaces prefixing a line.
	*/
	public static int getIndent(String line) {
		return getIndent(line, ' ');
	}

	/**
	** Strip {@code p} tags from single-line comments, such as those used in the
	** method summary table.
	**
	** @param ppre The exact form of {@code <p>} that is rendered (e.g. {@code
	**        "<P>\n"}). This will be removed from the start of the string.
	** @param psuf The exact form of {@code </p>} that is rendered (e.g. {@code
	**        "</P>\n"}). This will be removed from the end of the string.
	** @param s The string to strip if appropriate.
	*/
	public static String stripSingleP(String ppre, String psuf, String s) {
		return (s.lastIndexOf(ppre) == 0 && s.endsWith(psuf))? s.substring(ppre.length(), s.length()-psuf.length()): s;
	}

}
