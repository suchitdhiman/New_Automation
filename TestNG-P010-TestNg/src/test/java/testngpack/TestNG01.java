package testngpack;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;

public class TestNG01 {
  @Test
  public void f() {
	  System.out.println("TestNG01.f()");
  }
  @BeforeMethod
  public void beforeMethod() {
	  System.out.println("TestNG01.beforeMethod()");
  }

  @AfterMethod
  public void afterMethod() {
	  System.out.println("TestNG01.afterMethod()");
  }

  @BeforeClass
  public void beforeClass() {
	  System.out.println("TestNG01.beforeClass()");
  }

  @AfterClass
  public void afterClass() {
	  System.out.println("TestNG01.afterClass()");
  }

  @BeforeTest
  public void beforeTest() {
	  System.out.println("TestNG01.beforeTest()");
  }

}
