package com.sk.tests;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.Assert;
import org.apache.log4j.Logger;

public class AuthenticationTests extends BaseClass {

    private static Logger logger = Logger.getLogger(AuthenticationTests.class);

    @BeforeSuite
    public void beforeSuite() throws Exception {
        logger.info("========== Authentication Test Suite Started ==========");
        init();
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        // One Extent node per parallel thread group — just a label
        extentReports.createTest("Authentication Test Suite - " + Thread.currentThread().getName());
    }

    @AfterClass
    public void afterClass() {
        logger.info("========== Authentication Test Suite Ended (thread: "
            + Thread.currentThread().getName() + ") ==========");
    }

    @AfterSuite
    public void afterSuite() {
        if (extentReports != null) {
            extentReports.flush();  // exactly once, after ALL threads finish
        }
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        logger.info("Launching Browser...");
        browserLauncher();
        selectUrl("applicationUrl");
    }

    @AfterMethod
    public void afterMethod() {
        logger.info("Test execution completed. Closing browser...");
        quitBrowser();
    }

    // ==================== TEST CASE 1: Valid Login ====================
    @Test(priority = 1, description = "Verify user can login with valid credentials")
    public void testValidLogin() {
        try {
            logger.info("TEST: Valid Login - Starting");
            setExtentTest(extentReports.createTest("Test - Valid Login"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", "student");
            logger.info("Username entered");

            typeText("password_id", "Password123");
            logger.info("Password entered");

            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isLoggedIn = isElementPresent("dashboard_xpath");
            Assert.assertTrue(isLoggedIn, "User should be logged in successfully");

            logger.info("TEST: Valid Login - PASSED");
            getExtentTest().pass("User successfully logged in with valid credentials");

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Valid Login - FAILED: " + e.getMessage());
            getExtentTest().fail("Login failed: " + e.getMessage());
            Assert.fail("Valid Login test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 2: Invalid Username ====================
    @Test(priority = 2, description = "Verify login fails with invalid username")
    public void testInvalidUsername() {
        try {
            logger.info("TEST: Invalid Username - Starting");
            setExtentTest(extentReports.createTest("Test - Invalid Username"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", "invaliduser123");
            typeText("password_id", "Password123");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isErrorDisplayed = isElementPresent("error_message_xpath");
            Assert.assertTrue(isErrorDisplayed, "Error message should be displayed");

            String errorText = getText("error_message_xpath");
            Assert.assertTrue(errorText.contains("invalid") || errorText.contains("Invalid"),
                    "Error message should indicate invalid credentials");

            logger.info("TEST: Invalid Username - PASSED");
            getExtentTest().pass("Error message displayed for invalid username: " + errorText);

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Invalid Username - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("Invalid username test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 3: Invalid Password ====================
    @Test(priority = 3, description = "Verify login fails with invalid password")
    public void testInvalidPassword() {
        try {
            logger.info("TEST: Invalid Password - Starting");
            setExtentTest(extentReports.createTest("Test - Invalid Password"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", "student");
            typeText("password_id", "wrongPassword123");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isErrorDisplayed = isElementPresent("error_message_xpath");
            Assert.assertTrue(isErrorDisplayed, "Error message should be displayed");

            String errorText = getText("error_message_xpath");
            Assert.assertTrue(errorText.contains("invalid") || errorText.contains("Invalid"),
                    "Error message should indicate invalid password");

            logger.info("TEST: Invalid Password - PASSED");
            getExtentTest().pass("Error message displayed for invalid password: " + errorText);

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Invalid Password - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("Invalid password test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 4: Empty Username ====================
    @Test(priority = 4, description = "Verify login fails when username is empty")
    public void testEmptyUsername() {
        try {
            logger.info("TEST: Empty Username - Starting");
            setExtentTest(extentReports.createTest("Test - Empty Username"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("password_id", "Password123");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isErrorDisplayed = isElementPresent("error_message_xpath");
            Assert.assertTrue(isErrorDisplayed, "Error message should be displayed for empty username");

            logger.info("TEST: Empty Username - PASSED");
            getExtentTest().pass("Validation error displayed for empty username");

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Empty Username - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("Empty username test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 5: Empty Password ====================
    @Test(priority = 5, description = "Verify login fails when password is empty")
    public void testEmptyPassword() {
        try {
            logger.info("TEST: Empty Password - Starting");
            setExtentTest(extentReports.createTest("Test - Empty Password"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", "student");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isErrorDisplayed = isElementPresent("error_message_xpath");
            Assert.assertTrue(isErrorDisplayed, "Error message should be displayed for empty password");

            logger.info("TEST: Empty Password - PASSED");
            getExtentTest().pass("Validation error displayed for empty password");

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Empty Password - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("Empty password test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 6: SQL Injection Attempt ====================
    @Test(priority = 6, description = "Verify application handles SQL injection attempts")
    public void testSQLInjectionAttempt() {
        try {
            logger.info("TEST: SQL Injection Attempt - Starting");
            setExtentTest(extentReports.createTest("Test - SQL Injection Attempt"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", "' OR '1'='1");
            typeText("password_id", "' OR '1'='1");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isLoggedIn = isElementPresent("dashboard_xpath");
            Assert.assertFalse(isLoggedIn, "SQL injection should not bypass authentication");

            logger.info("TEST: SQL Injection Attempt - PASSED (Security verified)");
            getExtentTest().pass("Application correctly rejected SQL injection attempt");

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: SQL Injection Attempt - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("SQL injection test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 7: Case Sensitivity ====================
    @Test(priority = 7, description = "Verify login with different case combinations")
    public void testCaseSensitivity() {
        try {
            logger.info("TEST: Case Sensitivity - Starting");
            setExtentTest(extentReports.createTest("Test - Case Sensitivity"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", "STUDENT");
            typeText("password_id", "Password123");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isLoggedIn = isElementPresent("dashboard_xpath");
            boolean isErrorDisplayed = isElementPresent("error_message_xpath");

            Assert.assertTrue(isLoggedIn || isErrorDisplayed,
                    "System should either accept or reject uppercase username");

            logger.info("TEST: Case Sensitivity - PASSED");
            getExtentTest().pass("Case sensitivity behavior verified");

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Case Sensitivity - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("Case sensitivity test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 8: Logout Functionality ====================
    @Test(priority = 8, description = "Verify user can logout successfully")
    public void testLogout() {
        try {
            logger.info("TEST: Logout - Starting");
            setExtentTest(extentReports.createTest("Test - Logout"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);
            typeText("username_id", "student");
            typeText("password_id", "Password123");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isLoggedIn = isElementPresent("dashboard_xpath");
            Assert.assertTrue(isLoggedIn, "User should be logged in");

            clickElement("logout_btn_xpath");
            Thread.sleep(2000);

            boolean isLoginPageDisplayed = isElementPresent("signin_btn_id");
            Assert.assertTrue(isLoginPageDisplayed, "Should return to login page after logout");

            logger.info("TEST: Logout - PASSED");
            getExtentTest().pass("User successfully logged out and returned to login page");

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Logout - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("Logout test failed: " + e.getMessage());
        }
    }

    // ==================== TEST CASE 9: Login with Special Characters ====================
    @Test(priority = 9, description = "Verify login behavior with special characters")
    public void testSpecialCharactersInPassword() {
        try {
            logger.info("TEST: Special Characters - Starting");
            setExtentTest(extentReports.createTest("Test - Special Characters"));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", "student");
            typeText("password_id", "!@#$%^&*()");
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isErrorDisplayed = isElementPresent("error_message_xpath");
            Assert.assertTrue(isErrorDisplayed, "Error should be displayed for wrong password");

            logger.info("TEST: Special Characters - PASSED");
            getExtentTest().pass("Special characters in password handled correctly");

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Special Characters - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed: " + e.getMessage());
            Assert.fail("Special characters test failed: " + e.getMessage());
        }
    }

    // ==================== DATA-DRIVEN TEST: Multiple Credentials ====================
    @Test(priority = 10, dataProvider = "loginCredentials",
           description = "Data-driven test with multiple username/password combinations")
    public void testLoginWithMultipleCredentials(String username, String password, boolean shouldSucceed) {
        try {
            logger.info("TEST: Data-driven Login with username: " + username);
            setExtentTest(extentReports.createTest("Test - Login with " + username));

            clickElement("login_btn_xpath");
            Thread.sleep(1000);

            typeText("username_id", username);
            typeText("password_id", password);
            clickElement("signin_btn_id");
            Thread.sleep(2000);

            boolean isLoggedIn = isElementPresent("dashboard_xpath");

            if (shouldSucceed) {
                Assert.assertTrue(isLoggedIn, "Login should succeed for: " + username);
                logger.info("TEST: Data-driven Login - PASSED");
                getExtentTest().pass("Successfully logged in with: " + username);
            } else {
                boolean isErrorDisplayed = isElementPresent("error_message_xpath");
                Assert.assertTrue(isErrorDisplayed, "Error should be displayed for: " + username);
                logger.info("TEST: Data-driven Login - PASSED (Expected failure)");
                getExtentTest().pass("Login correctly failed for invalid credentials: " + username);
            }

        } catch (AssertionError | InterruptedException e) {
            logger.error("TEST: Data-driven Login - FAILED: " + e.getMessage());
            getExtentTest().fail("Test failed for username " + username + ": " + e.getMessage());
            Assert.fail("Data-driven login test failed for " + username + ": " + e.getMessage());
        }
    }

    // ==================== DATA PROVIDER ====================
    @DataProvider(name = "loginCredentials")
    public Object[][] loginCredentials() {
        return new Object[][] {
            {"student", "Password123", true},        // Valid login
            {"invaliduser", "password123", false},   // Invalid username
            {"student", "wrongpassword", false},     // Invalid password
            {"", "Password123", false},              // Empty username
            {"student", "", false},                  // Empty password
            {"admin", "admin", false},               // Wrong credentials
            {"testuser", "Test@123", false},         // Non-existent user
        };
    }
}