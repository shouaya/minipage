package com.jialu.minipage;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MiniCell {
	private List<String> classes;
	private List<String> styles;
	private String content;
	private String html;
	private MiniFont font;

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
		String html = "<div class=\"%s\" style=\"%s\"></div>%s";
		if (content == null) {
			content = "";
		} else {
			content = "<div class=\"%s\" style=\"%s;\">" + content + "</div>";
			content = String.format(content, StringUtils.join(this.font.getClasses(), " "),
					StringUtils.join(this.font.getStyles(), ";"), content);
		}
		this.html = String.format(html, StringUtils.join(this.classes, " "), StringUtils.join(this.styles, ";"),
				content);
	}

	public MiniFont getFont() {
		return font;
	}

	public void setFont(MiniFont font) {
		this.font = font;
	}

}
