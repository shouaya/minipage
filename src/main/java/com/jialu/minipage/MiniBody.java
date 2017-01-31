package com.jialu.minipage;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MiniBody {

	private String html;
	private HashMap<String, String> css;
	private LinkedHashMap<String, MiniCell> cells;
	
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public HashMap<String, String> getCss() {
		return css;
	}
	public void setCss(HashMap<String, String> css) {
		this.css = css;
	}
	public LinkedHashMap<String, MiniCell> getCells() {
		return cells;
	}
	public void setCells(LinkedHashMap<String, MiniCell> cells) {
		this.cells = cells;
	}
}
