package testngpack.listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;

import testngpack.base.ConfigReader;
import testngpack.base.ExtentManager;
import testngpack.utils.Log;

/**
 * Suite level hooks. Flushing Extent here (rather than in an @AfterMethod)
 * means the html report is written once, after every parallel thread is done.
 */
public class SuiteListener implements ISuiteListener {

	@Override
	public void onStart(ISuite suite) {
		ConfigReader.init();
		Log.info("===== Suite '" + suite.getName() + "' started, parallel="
				+ suite.getXmlSuite().getParallel() + ", thread-count="
				+ suite.getXmlSuite().getThreadCount() + " =====");
	}

	@Override
	public void onFinish(ISuite suite) {
		ExtentManager.flush();
		Log.info("===== Suite '" + suite.getName() + "' finished. Report: "
				+ ConfigReader.PROJECT_DIR + "/Report/htmlReport.html =====");
	}
}
