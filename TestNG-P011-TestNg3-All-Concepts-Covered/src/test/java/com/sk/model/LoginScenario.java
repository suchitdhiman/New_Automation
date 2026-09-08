package com.sk.model;

/**
 * One row of the {@code LoginData} sheet.
 *
 * <p>A DataProvider that hands back {@code Object[][]} of loose Strings works,
 * but the test method then reads {@code (String) row[3]} and nobody remembers
 * what column 3 was. Mapping the row onto a record costs ten lines here and
 * makes every data-driven test self-documenting.
 */
public record LoginScenario(
		String testCaseId,
		String username,
		String password,
		boolean shouldLogin,
		String expectedMessage,
		boolean execute) {

	/** Used as the TestNG test name so the report shows the case id, not "row 4". */
	@Override
	public String toString() {
		return testCaseId + " [" + username + "]";
	}
}
