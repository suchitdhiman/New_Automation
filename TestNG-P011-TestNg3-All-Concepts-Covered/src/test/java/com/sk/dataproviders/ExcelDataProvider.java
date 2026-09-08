package com.sk.dataproviders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;

import com.sk.core.ConfigManager;
import com.sk.model.CheckoutScenario;
import com.sk.model.LoginScenario;
import com.sk.utils.ExcelReader;

/**
 * Spreadsheet-driven data.
 *
 * <p>The workbook lives at {@code src/test/resources/testdata/SwagLabsTestData.xlsx}
 * and a business analyst can edit it without touching Java. That is the whole
 * argument for Excel-driven testing, and it only survives contact with reality
 * if the framework does three things, all of which happen here:
 *
 * <ol>
 *   <li><b>Honour an Execute column.</b> Rows are switched off in the sheet, not
 *       commented out in code.</li>
 *   <li><b>Map columns by name, never by index</b> - see {@link ExcelReader}.</li>
 *   <li><b>Fail loudly on an empty result.</b> A provider that returns zero rows
 *       makes TestNG report "0 tests run", which looks like a pass in most CI
 *       dashboards. Throwing a {@link SkipException} instead puts a visible
 *       SKIPPED in the report.</li>
 * </ol>
 */
public final class ExcelDataProvider {

	private static final Logger LOG = LogManager.getLogger(ExcelDataProvider.class);

	public static final String SHEET_LOGIN = "LoginData";
	public static final String SHEET_CHECKOUT = "CheckoutData";

	private ExcelDataProvider() {
		// static provider holder
	}

	@DataProvider(name = "excelLoginData")
	public static Iterator<Object[]> excelLoginData() {
		List<Map<String, String>> rows = read(SHEET_LOGIN);

		List<Object[]> scenarios = rows.stream()
				.filter(row -> ExcelReader.flag(row, "Execute"))
				.map(row -> new Object[] { new LoginScenario(
						ExcelReader.require(row, "TestCaseId"),
						ExcelReader.optional(row, "Username", ""),
						ExcelReader.optional(row, "Password", ""),
						ExcelReader.flag(row, "ShouldLogin"),
						ExcelReader.optional(row, "ExpectedMessage", ""),
						true) })
				.toList();

		guardAgainstEmpty(scenarios.size(), SHEET_LOGIN);
		LOG.info("Sheet [{}]: {} of {} row(s) marked Execute=Y", SHEET_LOGIN, scenarios.size(), rows.size());
		return scenarios.iterator();
	}

	/**
	 * Same sheet, run in parallel. The rows are independent - each gets its own
	 * browser off the ThreadLocal - so there is nothing to synchronise.
	 */
	@DataProvider(name = "excelLoginDataParallel", parallel = true)
	public static Iterator<Object[]> excelLoginDataParallel() {
		return excelLoginData();
	}

	@DataProvider(name = "excelCheckoutData")
	public static Iterator<Object[]> excelCheckoutData() {
		List<Map<String, String>> rows = read(SHEET_CHECKOUT);

		List<Object[]> scenarios = rows.stream()
				.filter(row -> ExcelReader.flag(row, "Execute"))
				.map(row -> new Object[] { new CheckoutScenario(
						ExcelReader.require(row, "TestCaseId"),
						ExcelReader.optional(row, "FirstName", ""),
						ExcelReader.optional(row, "LastName", ""),
						ExcelReader.optional(row, "ZipCode", ""),
						ExcelReader.flag(row, "ShouldSucceed"),
						ExcelReader.optional(row, "ExpectedError", ""),
						true) })
				.toList();

		guardAgainstEmpty(scenarios.size(), SHEET_CHECKOUT);
		LOG.info("Sheet [{}]: {} of {} row(s) marked Execute=Y", SHEET_CHECKOUT, scenarios.size(), rows.size());
		return scenarios.iterator();
	}

	private static List<Map<String, String>> read(String sheet) {
		return ExcelReader.read(ConfigManager.get("excel.testdata.path"), sheet);
	}

	private static void guardAgainstEmpty(int count, String sheet) {
		if (count == 0) {
			throw new SkipException("No row in sheet [" + sheet + "] has Execute=Y, so there is nothing to run.");
		}
	}
}
