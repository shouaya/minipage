package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.io.Resources;

public class MiniPage {

	// TODO 不同device对应的css
	public static void main(String[] args) throws IOException {
		if (args.length != 3 && args.length != 4) {
			System.out.println("Parameters Size Error");
			throw new RuntimeException();
		}
		System.setProperty("webdriver.chrome.driver", args[1]);
		XSSFWorkbook book = readDesignFile(args[0]);
		String indexPath = "";
		if (args.length == 4) {
			indexPath = createHtmlFile(book.getSheet(args[3]));
		} else {
			indexPath = createHtmlFile(book);
		}
		previewHtmlFile(indexPath, args[2]);
	}

	private static String createHtmlFile(XSSFSheet sheet) throws IOException {
		StringBuilder sb = new StringBuilder();
		String headContent = Resources.toString(Resources.getResource("header.html"),
				Charset.forName(CharEncoding.UTF_8));
		sb.append(headContent);
		String bodyContent = createBodyContent(sheet);
		sb.append(bodyContent);
		String footContent = Resources.toString(Resources.getResource("footer.html"),
				Charset.forName(CharEncoding.UTF_8));
		sb.append(footContent);
		File file = new File(String.format("out/%s.html", sheet.getSheetName()));
		FileUtils.writeStringToFile(file, sb.toString(), Charset.forName(CharEncoding.UTF_8));
		return file.getCanonicalPath();
	}

	private static void previewHtmlFile(String path, String device) {
		Map<String, String> mobileEmulation = new HashMap<String, String>();
		mobileEmulation.put("deviceName", device);
		Map<String, Object> chromeOptions = new HashMap<String, Object>();
		chromeOptions.put("mobileEmulation", mobileEmulation);
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		WebDriver driver = new ChromeDriver(capabilities);
		driver.get(path);
	}

	private static String createHtmlFile(XSSFWorkbook book) throws IOException {
		int cntSheet = book.getNumberOfSheets();
		String indexPath = "";
		for (int index = 0; index < cntSheet; index++) {
			String path = createHtmlFile(book.getSheetAt(index));
			if (index == 0) {
				indexPath = path;
			}
		}
		return indexPath;
	}

	private static String createBodyContent(XSSFSheet xssfSheet) throws IOException {
		StringBuilder sb = new StringBuilder();
		int maxRow = 25;
		int maxCol = 23;
		for (int row = 0; row < maxRow; row++) {
			for (int col = 0; col < maxCol; col++) {
				XSSFCell right = col < maxCol - 1 ? xssfSheet.getRow(row).getCell(col + 1) : null;
				XSSFCell bottom = row < maxRow - 1 ? xssfSheet.getRow(row + 1).getCell(col) : null;
				MiniCell cell = converCell(xssfSheet.getRow(row).getCell(col), right, bottom);
				sb.append(cell.getHtml());
			}
		}
		return sb.toString();
	}

	private static MiniCell converCell(XSSFCell cell, XSSFCell right, XSSFCell bottom) {
		MiniCell mc = new MiniCell();
		mc.setHtml("");
		mc.setClasses(new ArrayList<String>());
		mc.setStyles(new ArrayList<String>());
		if (cell == null) {
			return mc;
		}
		int row = cell.getRowIndex();
		int col = cell.getColumnIndex();

		XSSFColor bgColor = cell.getCellStyle().getFillForegroundColorColor();
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
				mc.getClasses().add(" BB" + getCssBorder(cell.getCellStyle().getBorderBottomEnum()));
			}
		}
		if (cell.toString() == "" && mc.getClasses().size() == 0 && bgColor == null) {
			return mc;
		}
		mc.getClasses().add("R");
		mc.getClasses().add("C");
		mc.getClasses().add("R" + row);
		mc.getClasses().add("C" + col);
		if (bgColor != null) {
			String cssColor = bgColor.getARGBHex().substring(2);
			mc.getStyles().add("background-color:#" + cssColor);
		}
		if (cell.toString() != "") {
			int pt = cell.getCellStyle().getFont().getFontHeightInPoints();
			mc.setBold(cell.getCellStyle().getFont().getBold());
			mc.setSize(pt * 3 / 4 + 3);
			mc.setContent(cell.getStringCellValue());
		}
		mc.creatHtml(row, col);
		return mc;
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
