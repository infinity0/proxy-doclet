package proxy.adapters.markup;

import proxy.adapters.MarkupRenderer;
import proxy.adapters.Markup;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.IOException;

/**
** {@link MarkupRenderer} that uses pandoc to render pandoc markdown in javadoc
** comments to HTML.
**
** TODO find a way of calling the Haskell library directly from Java
**
** @author infinity0
*/
public class PandocMarkdownRenderer implements MarkupRenderer {

	public String render(String text) {
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(new String[]{"pandoc"});

			OutputStream out = proc.getOutputStream();
			new PrintStream(out).print(text);
			out.close();

			StringWriter str = new StringWriter(text.length());
			InputStreamReader in = new InputStreamReader(proc.getInputStream());
			for (int c = in.read(); c != -1; c = in.read()) { str.write(c); }

			return Markup.stripSingleP("<p\n>", "</p\n>\n", str.toString());

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			if (proc != null) { proc.destroy(); }
		}

	}

}
