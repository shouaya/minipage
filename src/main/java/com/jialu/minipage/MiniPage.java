package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.google.common.io.Resources;

public class MiniPage {

	private static final int MAX_ROW_CNT = 35;
	private static final int MAX_COL_CNT = 20;
	private static final double ROW_HEIGHT = 1;
	private static final double COL_WIDTH = 1;

	// TODO 不同device对应的css、html控件支持
	public static void main(String[] args) throws IOException {
		if (args.length != 1 && args.length != 2) {
			System.out.println("Parameters Size Error");
			throw new RuntimeException();
		}
		XSSFWorkbook book = readDesignFile(args[0]);
		if (args.length == 2) {
			createHtmlFile(book.getSheet(args[1]));
		} else {
			createHtmlFile(book);
		}
		System.out.println("HTMLを生成しました。");
	}

	private static String createHtmlFile(XSSFSheet sheet) throws IOException {
		StringBuilder sb = new StringBuilder();
		String bodyContent = createBodyContent(sheet);
		sb.append(bodyContent).append("\r\n");
		File file = new File(String.format("out/%s.html", sheet.getSheetName()));
		FileUtils.writeStringToFile(file, sb.toString(), Charset.forName(CharEncoding.UTF_8));
		return file.getCanonicalPath();
	}

	private static String createHtmlFile(XSSFWorkbook book) throws IOException {
		int cntSheet = book.getNumberOfSheets();
		String indexPath = "";
		for (int index = 0; index < cntSheet; index++) {
			XSSFSheet sheet = book.getSheetAt(index);
			if(sheet.getSheetName().startsWith("_")){
				continue;
			}
			String path = createHtmlFile(sheet);
			if (index == 0) {
				indexPath = path;
			}
		}
		return indexPath;
	}

	private static String createBodyContent(XSSFSheet xssfSheet) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(xssfSheet.getSheetName()).append(">\r\n");
		for (int row = 0; row < MAX_ROW_CNT; row++) {
			for (int col = 0; col < MAX_COL_CNT; col++) {
				XSSFCell right = col < MAX_COL_CNT - 1 ? xssfSheet.getRow(row).getCell(col + 1) : null;
				XSSFCell bottom = row < MAX_ROW_CNT - 1 ? xssfSheet.getRow(row + 1).getCell(col) : null;
				MiniCell cell = converCell(xssfSheet.getRow(row).getCell(col), right, bottom,
						xssfSheet.getMergedRegions());
				String html = cell.getHtml();
				sb.append(html);
				if (html.length() > 0)
					sb.append("\r\n");
			}
		}
		String scriptPath = "javascript/" + xssfSheet.getSheetName() + ".js";
		URL p = MiniPage.class.getClassLoader().getResource(scriptPath);
		if (p != null) {
			String script = Resources.toString(Resources.getResource(scriptPath), Charset.forName(CharEncoding.UTF_8));
			sb.append("\r\n<script>\r\n").append(script).append("\r\n</script>\r\n");
		}

		sb.append("</").append(xssfSheet.getSheetName()).append(">");
		return sb.toString();
	}

	private static MiniCell converCell(XSSFCell cell, XSSFCell right, XSSFCell bottom, List<CellRangeAddress> list) {
		MiniCell mc = new MiniCell();

		if (cell == null) {
			return mc;
		}
		drawCellBorder(mc, cell, right, bottom, list);

		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();
		if (cell.toString() == "" && mc.getClasses().size() == 0 && bgColor == null) {
			return mc;
		}

		drawCellContent(mc, cell, list);

		return mc;
	}

	@SuppressWarnings("deprecation")
	private static void drawCellContent(MiniCell mc, XSSFCell cell, List<CellRangeAddress> list) {
		mc.getClasses().add("R");
		mc.getClasses().add("C");
		mc.getClasses().add("R" + cell.getRowIndex());
		mc.getClasses().add("C" + cell.getColumnIndex());
		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();
		if (bgColor != null) {
			String cssColor = bgColor.getARGBHex().substring(2);
			mc.getStyles().add("background-color:#" + cssColor);
		}

		if (cell.toString() == "") {
			mc.creatHtml();
			return;
		}
		mc.setFont(getMiniFont(cell, list));
		switch (cell.getCellType()) {
		case HSSFCell.CELL_TYPE_FORMULA:
			mc.setContent(getControlByCell(cell));
			break;
		default:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			mc.setContent("<pre>" + cell.getStringCellValue() + "</pre>");
			break;
		}
		mc.creatHtml();
	}

	private static String getControlByCell(XSSFCell cell) {
		CellReference ref = new CellReference(cell.getCellFormula());
		XSSFRow row = cell.getSheet().getRow(ref.getRow());
		String inId = row.getCell(24).toString();
		String inName = row.getCell(25).toString();
		String inType = row.getCell(26).toString();
		String inValue = row.getCell(27).toString();
		String inClass = row.getCell(28).toString();
		String inClick = row.getCell(29).toString();
		return String.format("<input id=\"%s\"  name=\"%s\"  type=\"%s\"  value=\"%s\" class=\"%s\" onclick={%s}/>",
				inId, inName, inType, inValue, inClass, inClick);
	}

	private static void drawCellBorder(MiniCell mc, XSSFCell cell, XSSFCell right, XSSFCell bottom,
			List<CellRangeAddress> list) {
		if (isInMergedRange(cell, list)) {
			drawMergedCellBorder(mc, cell, list, right, bottom);
			return;
		}
		if (cell.getCellStyle().getBorderLeftEnum().getCode() > 0) {
			mc.getClasses().add("BL" + getCssBorder(cell.getCellStyle().getBorderLeftEnum()));
		}
		if (cell.getCellStyle().getBorderRightEnum().getCode() > 0) {
			if (right == null || right.getCellStyle().getBorderLeftEnum().getCode() <= 0) {
				mc.getClasses().add("BR" + getCssBorder(cell.getCellStyle().getBorderRightEnum()));
			}
		}
		if (cell.getCellStyle().getBorderTopEnum().getCode() > 0) {
			mc.getClasses().add("BT" + getCssBorder(cell.getCellStyle().getBorderTopEnum()));
		}
		if (cell.getCellStyle().getBorderBottomEnum().getCode() > 0) {
			if (bottom == null || bottom.getCellStyle().getBorderTopEnum().getCode() <= 0) {
				mc.getClasses().add("BB" + getCssBorder(cell.getCellStyle().getBorderBottomEnum()));
			}
		}
	}

	private static void drawMergedCellBorder(MiniCell mc, XSSFCell cell, List<CellRangeAddress> list, XSSFCell right,
			XSSFCell bottom) {
		boolean isLeftCell = isLeftCellInMergedRange(cell, list);
		if (isLeftCell && cell.getCellStyle().getBorderLeftEnum().getCode() > 0) {
			mc.getClasses().add("BL" + getCssBorder(cell.getCellStyle().getBorderLeftEnum()));
		}
		boolean isBottomCell = isBottomCellInMergedRange(cell, list);
		if (isBottomCell && cell.getCellStyle().getBorderBottomEnum().getCode() > 0) {
			if (bottom == null || bottom.getCellStyle().getBorderTopEnum().getCode() <= 0) {
				mc.getClasses().add("BB" + getCssBorder(cell.getCellStyle().getBorderBottomEnum()));
			}
		}
		boolean isTopCell = isTopCellInMergedRange(cell, list);
		if (isTopCell && cell.getCellStyle().getBorderTopEnum().getCode() > 0) {
			mc.getClasses().add("BT" + getCssBorder(cell.getCellStyle().getBorderTopEnum()));
		}
		boolean isRightCell = isRightCellInMergedRange(cell, list);
		if (isRightCell && cell.getCellStyle().getBorderRightEnum().getCode() > 0) {
			if (right == null || right.getCellStyle().getBorderLeftEnum().getCode() <= 0) {
				mc.getClasses().add("BR" + getCssBorder(cell.getCellStyle().getBorderRightEnum()));
			}
		}
	}

	private static boolean isRightCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getLastColumn() == cell.getColumnIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isBottomCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getLastRow() == cell.getRowIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTopCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstRow() == cell.getRowIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isLeftCellInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstColumn() == cell.getColumnIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isInMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.containsColumn(cell.getColumnIndex()) && range.containsRow(cell.getRowIndex())) {
				return true;
			}
		}
		return false;
	}

	private static MiniFont getMiniFont(XSSFCell cell, List<CellRangeAddress> list) {
		MiniFont font = new MiniFont();
		font.setClasses(new ArrayList<String>());
		font.setStyles(new ArrayList<String>());
		font.getClasses().add("I");
		font.getClasses().add("R" + cell.getRowIndex());
		font.getClasses().add("C" + cell.getColumnIndex());
		if (cell.getCellStyle().getFont().getBold()) {
			font.getClasses().add("IB");
		}
		XSSFCellStyle style = cell.getCellStyle();
		XSSFFont xssfont = style.getFont();
		XSSFColor colour = xssfont.getXSSFColor();
		if (colour != null) {
			font.getStyles().add("color:#" + colour.getARGBHex().substring(2));
		}
		int pt = cell.getCellStyle().getFont().getFontHeightInPoints();
		font.getStyles().add("font-size: " + (pt * 3 / 4 + 3) + "px");
		CellRangeAddress range = getMergedRange(cell, list);
		if (range == null) {
			return font;
		}
		font.getClasses().add("TA" + style.getAlignmentEnum().getCode());
		font.getClasses().add("VA" + style.getVerticalAlignmentEnum().getCode());
		int width = (int) ((range.getLastColumn() - range.getFirstColumn() + 1) * COL_WIDTH);
		int height = (int) ((range.getLastRow() - range.getFirstRow() + 1) * ROW_HEIGHT);
		font.getStyles().add("width: " + width + "rem");
		font.getStyles().add("height: " + height + "rem");
		return font;
	}

	private static CellRangeAddress getMergedRange(XSSFCell cell, List<CellRangeAddress> list) {
		for (CellRangeAddress range : list) {
			if (range.getFirstRow() == cell.getRowIndex() && range.getFirstColumn() == cell.getColumnIndex()) {
				return range;
			}
		}
		return null;
	}

	private static String getCssBorder(BorderStyle borderStyle) {
		if (borderStyle == BorderStyle.THIN) {
			return "3";
		} else if (borderStyle == BorderStyle.MEDIUM) {
			return "3";
		} else if (borderStyle == BorderStyle.DASHED) {
			return "1";
		} else if (borderStyle == BorderStyle.DOTTED) {
			return "1";
		} else if (borderStyle == BorderStyle.THICK) {
			return "1";
		} else if (borderStyle == BorderStyle.DOUBLE) {
			return "4";
		} else if (borderStyle == BorderStyle.HAIR) {
			return "1";
		} else if (borderStyle == BorderStyle.MEDIUM_DASHED) {
			return "2";
		} else if (borderStyle == BorderStyle.DASH_DOT) {
			return "1";
		} else if (borderStyle == BorderStyle.MEDIUM_DASH_DOT) {
			return "2";
		} else if (borderStyle == BorderStyle.DASH_DOT_DOT) {
			return "1";
		} else if (borderStyle == BorderStyle.MEDIUM_DASH_DOT_DOT) {
			return "2";
		} else if (borderStyle == BorderStyle.SLANTED_DASH_DOT) {
			return "2";
		}
		return "";
	}

	private static XSSFWorkbook readDesignFile(String file) throws IOException {
		FileInputStream execlFileStream = null;
		XSSFWorkbook book = null;

		try {
			execlFileStream = new FileInputStream(new File(file));
			book = new XSSFWorkbook(execlFileStream);
			execlFileStream.close();
		} catch (IOException e) {
			System.out.println("Can Not Read File:" + e.getMessage());
			throw e;
		} finally {
			execlFileStream.close();
		}
		return book;
	}
}
