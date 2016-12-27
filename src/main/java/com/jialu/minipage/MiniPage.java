package com.jialu.minipage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.io.Resources;

public class MiniPage {

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
		System.out.println(xssfSheet.getSheetName());
		StringBuilder sb = new StringBuilder();
		int maxRow = 24;
		int maxCol = 23;
		for (int row = 0; row < maxRow; row++) {
			for (int col = 0; col < maxCol; col++) {
				String cell = converCellToString(xssfSheet.getRow(row).getCell(col));
				sb.append(cell);
				System.out.println(cell);
			}
		}
		return sb.toString();
	}

	private static String converCellToString(XSSFCell cell) {
		if (cell == null) {
			return "";
		}
		if (cell.getRawValue() == "") {
			return "";
		}
		System.out.println(cell.getCellStyle().getDataFormatString());
		String stlye = "<div class='R C R%d C%d'>%s</div>";
		int row = cell.getRowIndex() + 1;
		int col = cell.getColumnIndex() + 1;
		return String.format(stlye, row, col, cell.getRawValue());
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
