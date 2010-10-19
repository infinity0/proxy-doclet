package proxy.adapters;

import proxy.adapters.markup.*;

/**
** Static utility methods for markup-related adapters.
**
** @author infinity0
*/
final public class Markup {

	final private static MarkupRenderer renderer;

	private Markup() { }

	static {
		//renderer = new PreformatRenderer();
		//renderer = new MediaWikiRenderer();
		renderer = new PandocMarkdownRenderer();
	}

	/**
	** Render the given text using the system-wide renderer. This is obtained
	** from the {@code proxy.adapters.markup} system property. TODO currently
	** it assumes MediaWiki syntax.
	*/
	public static String render(String text) {
		return renderer.render(deindent(text));
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
	public static String deindent(String text) {
		// TODO make this more efficient

		String lines[] = text.split("\\n");
		int indent=Integer.MAX_VALUE;
		// find smallest indent in the first 4 non-blank lines, excl the first line
		// whose indent is discarded by javadoc
		for (int i=1, l=0; i<lines.length && l<4; ++i) {
			int j=-1;
			while (++j < lines[i].length() && lines[i].charAt(j) == ' ');
			if (j == lines[i].length()) { continue; }
			if (j < indent) { indent = j; }
			++l;
		}

		StringBuffer result = new StringBuffer();
		for (int i=0; i<lines.length; ++i) {
			int j=-1;
			while (++j < lines[i].length() && lines[i].charAt(j) == ' ');
			// strip the indent (or smaller) from each line
			result.append(lines[i].substring(indent > j? j: indent)).append('\n');
		}
		return result.toString();

	}

	/**
	** Strips {@code p} tags from single-line comments, such as those
	** used in the method summary table.
	**
	** @param ppre The exact form of {@code <p>} that is rendered (e.g. {@code "<P>\n"})
	** @param psuf The exact form of {@code </p>} that is rendered (e.g. {@code "</P>\n"})
	** @param s The string to strip, if appropriate.
	*/
	public static String stripSingleP(String ppre, String psuf, String s) {
		return (s.lastIndexOf(ppre) == 0 && s.endsWith(psuf))? s.substring(ppre.length(), s.length()-psuf.length()): s;
	}

}
