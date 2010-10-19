package proxy.adapters;

/**
** @author infinity0
*/
public interface MarkupRenderer {

	/**
	** Render the markup contained in text. This should conform to the user's
	** expectations for the underlying doclet (e.g. HTML output).
	**
	** @param text Input text
	** @return Output text
	*/
	public String render(String text);

}
