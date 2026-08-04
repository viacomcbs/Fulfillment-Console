package com.paramount.test.ff.uitests.support;

import com.itextpdf.text.DocumentException;
import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.common.util.props.IProps.StaticProps;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class DownloadAllPDF extends BaseTest {

	@Factory(dataProvider = StaticProps.LATEST_BROWSERS_DATA_PROVIDER, dataProviderClass = DataProviderManager.class)
	public DownloadAllPDF(String runParams) {
		super.setRunParams(runParams);
	}

	@Severity(SeverityLevel.NORMAL)
	//@Features(Feature.class)
	@Test(groups = { "DownloadAllPDF" })
	public void downloadAllPDF() throws IOException, DocumentException {
		softAssert = new SoftAssert(new Object() {}.getClass().getEnclosingMethod().getName(), this.getClass().getSimpleName());

		String fileDirectory = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator;

		List<String> allFiles = softAssert.readFromFile(fileDirectory + "PDFMapping_Local.txt");
		System.out.println(allFiles);
		for (String line : allFiles) {
			softAssert.downloadFileFromServer(line.split("=")[0], line.split("=")[1]);
		}
	}

}