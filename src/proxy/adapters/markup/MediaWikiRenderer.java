package proxy.adapters.markup;

import proxy.adapters.MarkupRenderer;
import proxy.adapters.Markup;

import info.bliki.wiki.model.WikiModel;

/**
** {@link MarkupRenderer} that uses bliki-wiki to render MediaWiki markup in
** javadoc comments to HTML.
**
** It does some pre-processing first, though. Since paragraphs in javadoc comments
** are normally broken into lines using actual new-line characters, and since
** MediaWiki syntax has special semantics for lines that are incompatible this, the
** plaintext comments are first pre-processed to remove some of the line breaks
** (and some indent spaces) before passing it through the rendering engine.
**
** The algorithm is (omitting treatment of corner cases):
**
** - The "global indent" is calculated for the entire comment string; this is
**   stripped from all lines.
** - Each empty line starts a new paragraph. All lines in a paragraph are merged
**   together into a single line, except some special cases, depending on the type
**   of paragraph, which is determined by the first character after the global
**   indent, on the first line.
** - If the paragraph type is "text", all lines are merged.
** - If the paragraph type is "symbol", all lines are merged, except for lines
**   which start with that same symbol (lists, etc, in wiki syntax)
** - If the paragraph type is "space", all lines are merged, except for lines
**   which start with a space (preformatted text in wiki syntax)
**
** This results in some changes from the usual MediaWiki syntax. For example, you
** don't have to have to fit a list item on a single line; and you can indent it
** as you wish:
**
** ~~~~
** * This is a list item
**   and this is still the same
**   item as the previous lines
** * This is a new item
** ~~~~
**
** However, a more negative side effect is that you can't break up paragraphs with
** only a new entity (eg. an unordered list) - you must have an empty line to
** separate them, otherwise the pre-processor will concatenate the two lines and
** the rendering engine will see it as a single line. For example:
**
** ~~~~
** Lorem ipsum dolor sit amet.
**
** * This is a list item
** ~~~~
**
** not
**
** ~~~~
** Lorem ipsum dolor sit amet.
** * This is a list item
** ~~~~
**
** I've done it this way because it seems more natural; if you think otherwise, or
** if you have a better algorithm than the one stated, feel free to contact me with
** details of your thoughts.
**
** @author infinity0
*/
public class MediaWikiRenderer implements MarkupRenderer {

	 /** Wiki renderer */
	protected WikiModel wiki = new WikiModel("", "");

	public String render(String text) {
		return Markup.stripSingleP("\n<p>", "</p>", wiki.render(preprocessWikiString(text)));
	}

	public static String preprocessWikiString(String b) {
		String lines[] = b.split("\\n");
		StringBuffer result = new StringBuffer();
		boolean startedtext = false;
		boolean startblock = true;

		// blocktype ' ' : preformatted block
		// blocktype '\0' : normal paragraph
		// blocktype <symbol> : <symbol> type (e.g. * == ul, # == li)
		char blocktype = 0;


		// selectively merge lines into paragraphs
		for (String line: lines) {
			int j = Markup.getIndent(line);
			char firstchar = line.charAt(0);

			if (j == line.length()) {
				// if we haven't seen text yet, skip the entire line
				if (!startedtext) { continue; }
				// if line is empty, start a new paragraph
				result.append('\n').append('\n');
				startblock = true;
				continue;

			} else if (startblock) {
				// determine the automatic line-merge setting from the first character of each paragraph
				startblock = false;
				blocktype = Character.isLetter(firstchar)? 0: firstchar;

			} else if (firstchar == blocktype) {
				// start a new line when blocktype matches firstchar
				// when blocktype == '\0' for a normal paragraph this is effectively never
				result.append('\n');
			}

			startedtext = true;
			result.append(line).append(' ');
		}
		return result.toString();
	}

}
