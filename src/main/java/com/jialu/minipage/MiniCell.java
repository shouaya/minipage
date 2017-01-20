package com.jialu.minipage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author EB060
 *
 */
public class MiniCell {
	private List<String> classes;
	private List<String> styles;
	private String content;
	private String html;
	private MiniInner inner;

	public MiniCell() {
		this.setHtml("");
		this.setClasses(new ArrayList<String>());
		this.setStyles(new ArrayList<String>());
	}

	public List<String> getClasses() {
		return classes;
	}

	public void setClasses(List<String> classes) {
		this.classes = classes;
	}

	public List<String> getStyles() {
		return styles;
	}

	public void setStyles(List<String> styles) {
		this.styles = styles;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public void creatHtml() {
		if (this.content == null) {
			this.content = "";
		} else {
			if (this.inner.getClasses().size() > 0 && this.inner.getStyles().size() == 0) {
				this.content = String.format("<div class=\"%s\">%s</div>",
						StringUtils.join(this.inner.getClasses(), " "), content);
			} else if (this.inner.getClasses().size() == 0 && this.inner.getStyles().size() > 0) {
				this.content = String.format("<div style=\"%s;\">%s</div>",
						StringUtils.join(this.inner.getStyles(), ";"), content);
			} else if (this.inner.getClasses().size() == 0 && this.inner.getStyles().size() == 0) {
				this.content = String.format("<div><pre>%s</pre></div>", content);
			} else {
				this.content = String.format("<div class=\"%s\" style=\"%s;\">%s</div>",
						StringUtils.join(this.inner.getClasses(), " "), StringUtils.join(this.inner.getStyles(), ";"),
						content);
			}
		}
		if (this.classes.size() > 0 && this.styles.size() == 0) {
			this.html = String.format("<div class=\"%s\"></div>%s", StringUtils.join(this.classes, " "), content);
		} else if (this.classes.size() == 0 && this.styles.size() > 0) {
			this.html = String.format("<div style=\"%s;\"></div>%s", StringUtils.join(this.styles, ";"), content);
		} else if (this.classes.size() == 0 && this.styles.size() == 0) {
			this.html = String.format("<div></div>%s", content);
		} else {
			this.html = String.format("<div class=\"%s\" style=\"%s;\"></div>%s", StringUtils.join(this.classes, " "),
					StringUtils.join(this.styles, ";"), content);
		}

	}

	public MiniInner getInner() {
		return inner;
	}

	public void setInner(MiniInner font) {
		this.inner = font;
	}

}
