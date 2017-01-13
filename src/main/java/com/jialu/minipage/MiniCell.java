package com.jialu.minipage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MiniCell {
	private List<String> classes;
	private List<String> styles;
	private String content;
	private String html;
	private MiniFont font;

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
			if (this.font.getClasses().size() > 0 && this.font.getStyles().size() == 0) {
				this.content = String.format("<div class=\"%s\">%s</div>",
						StringUtils.join(this.font.getClasses(), " "), content);
			} else if (this.font.getClasses().size() == 0 && this.font.getStyles().size() > 0) {
				this.content = String.format("<div style=\"%s;\">%s</div>",
						StringUtils.join(this.font.getStyles(), ";"), content);
			} else if (this.font.getClasses().size() == 0 && this.font.getStyles().size() == 0) {
				this.content = String.format("<div></div>%s", content);
			} else {
				this.content = String.format("<div class=\"%s\" style=\"%s;\">%s</div>",
						StringUtils.join(this.font.getClasses(), " "), StringUtils.join(this.font.getStyles(), ";"),
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

	public MiniFont getFont() {
		return font;
	}

	public void setFont(MiniFont font) {
		this.font = font;
	}

}
