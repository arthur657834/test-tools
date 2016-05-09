package com.healthcloud.qa.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.formula.functions.Count;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.healthcloud.qa.utils.DB;
import com.healthcloud.qa.utils.DataReader;
import com.healthcloud.qa.utils.DataWriter;
import com.healthcloud.qa.utils.HTTPReqGen;
import com.healthcloud.qa.utils.RecordHandler;
import com.healthcloud.qa.utils.SheetUtils;
import com.healthcloud.qa.utils.StringUtil;
import com.jayway.restassured.response.Response;

import com.healthcloud.qa.utils.Property;
import com.healthcloud.qa.utils.Propertyfromyaml;
import com.healthcloud.qa.utils.Propertyfromyaml_org;

public class HTTPReqGenTest implements ITest {

	private Response response;
	private DataReader myInputData;
	private DataReader myBaselineData;
	private String template;

	public String getTestName() {
		return "API Test";
	}

	private String userDir = System.getProperty("user.dir");
	String filePath = "";
	String templatePath = userDir + File.separator + "http_request_template.txt";

	XSSFWorkbook wb = null;
	XSSFSheet inputSheet = null;
	XSSFSheet baselineSheet = null;
	XSSFSheet outputSheet = null;
	XSSFSheet comparsionSheet = null;
	XSSFSheet resultSheet = null;
	XSSFSheet loginSheet = null;

	private double totalcase = 0;
	private double failedcase = 0;
	private double successcase = 0;
	private String startTime = "";
	private String endTime = "";

	@BeforeTest
	@Parameters("workBook")
	public void setup(String path) {
		filePath = path;
		// System.out.println(userDir + File.separator);
		// System.out.println(userDir + File.separator + filePath);
		File file = new File(filePath);
		if (file.renameTo(file)) {
			System.out.println("文件未被操作");
		} else {
			System.out.println("文件正在被操作");
			JOptionPane.showMessageDialog(null, filePath + "文件正在被操作", "alert", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		try {
			wb = new XSSFWorkbook(new FileInputStream(filePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		inputSheet = wb.getSheet("Input");
		baselineSheet = wb.getSheet("Baseline");
		loginSheet = wb.getSheet("Login");

		SheetUtils.removeSheetByName(wb, "Output");
		SheetUtils.removeSheetByName(wb, "Comparison");
		SheetUtils.removeSheetByName(wb, "Result");
		outputSheet = wb.createSheet("Output");
		comparsionSheet = wb.createSheet("Comparison");
		resultSheet = wb.createSheet("Result");

		try {
			FileInputStream fis = new FileInputStream(new File(templatePath));
			template = IOUtils.toString(fis, Charset.defaultCharset());
		} catch (Exception e) {
			Assert.fail("Problem fetching data from input file:" + e.getMessage());
		}

		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime = sf.format(new Date());
	}

	@DataProvider(name = "WorkBookData")
	protected Iterator<Object[]> testProvider(ITestContext context) {

		List<Object[]> test_IDs = new ArrayList<Object[]>();

		myInputData = new DataReader(inputSheet, true, true, 0);

		// sort map in order so that test cases ran in a fixed order
		Map<String, RecordHandler> sortmap = new TreeMap<String, RecordHandler>(new Comparator<String>() {

			public int compare(String key1, String key2) {
				return key1.compareTo(key2);
			}

		});

		sortmap.putAll(myInputData.get_map());

		for (Map.Entry<String, RecordHandler> entry : sortmap.entrySet()) {
			String test_ID = entry.getKey();
			String test_case = entry.getValue().get("TestCase");
			System.out.println("test_ID:" + test_ID);
			System.out.println("test_case:" + test_case);
			System.out.println("call_suff:" + entry.getValue().get("call_suff"));

			if (!test_ID.equals("") && !test_case.equals("")) {
				test_IDs.add(new Object[] { test_ID, test_case });
			}
			totalcase++;
		}

		myBaselineData = new DataReader(baselineSheet, true, true, 0);

		return test_IDs.iterator();
	}

	@Test(dataProvider = "WorkBookData", description = "ReqGenTest")
	public void api_test(String ID, String test_case) throws Exception {

		HTTPReqGen myReqGen = new HTTPReqGen();

		try {
			myReqGen.generate_request(template, myInputData.get_record(ID));
			response = myReqGen.perform_request();
		} catch (Exception e) {
			Assert.fail("Problem using HTTPRequestGenerator to generate response: " + e.getMessage());
		}

		String baseline_message = myBaselineData.get_record(ID).get("Response");
		// System.out.println("baseline_message:" + baseline_message);
		if (filePath.contains("Login")) {
			JSONObject loginjsonObject = JSONObject.parseObject(baseline_message);
			System.out.println(loginjsonObject);
		}

		try {
			JSONObject jsonObject = JSONObject.parseObject(baseline_message);
			// yes, it is
		} catch (Exception e) {
			// no, it isnt
			Property readcnf = new Property();

			String DBtype = null;
			if ((baseline_message.split(":")[0].contains("mysql".toLowerCase()))
					| (baseline_message.split(":")[0].contains("mysql".toUpperCase()))) {
				baseline_message = baseline_message.substring(baseline_message.indexOf(":") + 1);
				DBtype = "mysql";

			} else if ((baseline_message.split(":")[0].contains("cassandra".toLowerCase()))
					| (baseline_message.split(":")[0].contains("cassandra".toUpperCase()))) {
				baseline_message = baseline_message.substring(baseline_message.indexOf(":") + 1);
				DBtype = "cassandra";
			} else if ((baseline_message.split(":")[0].contains("yaml".toLowerCase()))
					| (baseline_message.split(":")[0].contains("yaml".toUpperCase()))) {

				baseline_message = baseline_message.substring(baseline_message.indexOf(":") + 1);
				DBtype = "yaml";
			} else if (0 == baseline_message.indexOf("{")) {
				baseline_message = baseline_message;
			} else {
				System.out.println("DBtype未定义");
				JOptionPane.showMessageDialog(null, filePath + " DBtype未定义", "alert", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}

			JSONObject propertyfromyaml;
			if (baseline_message.contains("mysql")) {
				propertyfromyaml = Propertyfromyaml.propertyfromyaml("conf.yml", "mysql");
			} else {
				propertyfromyaml = Propertyfromyaml.propertyfromyaml("conf.yml", DBtype);
			}
			if (DBtype.contains("yaml")) {

				DB getbaselinefrommysql = new DB();

				baseline_message = getbaselinefrommysql.GetResult(DBtype, propertyfromyaml, baseline_message)
						.toString();
			} else {

				String SqlCharacter = (String) propertyfromyaml.get("SqlCharacter");
				String UrlSplit = (String) propertyfromyaml.get("UrlSplit");
				System.out.println("SqlCharacter:" + SqlCharacter);
				if (Pattern.compile(SqlCharacter.toLowerCase() + "|" + SqlCharacter.toUpperCase())
						.matcher(baseline_message).find()) {
					DB getbaselinefrommysql = new DB();
					baseline_message = getbaselinefrommysql.GetResult(DBtype, propertyfromyaml, baseline_message)
							.toString();
				}
			}

		}
		System.out.println("baseline_message:" + baseline_message);
		if (response.statusCode() == 200)
			try {
				DataWriter.writeData(outputSheet, response.asString(), ID, test_case);
				if (Pattern.compile("^\\[").matcher(response.asString()).find()) {
					JSONArray jsonArray_response = JSONArray.parseArray(response.asString());
					JSONArray jsonArray_baseline = JSONArray.parseArray(baseline_message);
					int conut=0;
					JSONCompareResult result = null ;
					for (int i_tmp = 0; i_tmp < jsonArray_response.size(); i_tmp++) {

					result = JSONCompare.compareJSON(
								StringUtil.removeSpaces(jsonArray_baseline.get(i_tmp).toString()),
								StringUtil.removeSpaces(jsonArray_response.get(i_tmp).toString()),
								JSONCompareMode.NON_EXTENSIBLE);
						if (!result.passed()) {
							DataWriter.writeData(comparsionSheet, result.getMessage(), ID, test_case);
							DataWriter.writeData(resultSheet, "false", ID, test_case, 0);
							conut++;


						} 
					}
					if (conut>0){
						System.out.println("0-------------------------------"+ID);
						failedcase++;
						Assert.fail(result.getMessage());
						
					}else{
						System.out.println("-------------------------------"+ID);
						DataWriter.writeData(resultSheet, "true", ID, test_case, 0);
					}
				} else {
					JSONCompareResult result = JSONCompare.compareJSON(StringUtil.removeSpaces(baseline_message),
							StringUtil.removeSpaces(response.asString()), JSONCompareMode.NON_EXTENSIBLE);

					if (!result.passed()) {
						DataWriter.writeData(comparsionSheet, result.getMessage(), ID, test_case);
						DataWriter.writeData(resultSheet, "false", ID, test_case, 0);
						failedcase++;
						System.out.println("failedcase:" + failedcase + "=========================================1");
						Assert.fail(result.getMessage());
					} else {
						DataWriter.writeData(resultSheet, "true", ID, test_case, 0);
						if(ID.contains("4")){
							System.out.println("--------------------2");
						}
					}
				}
			} catch (JSONException e) {
				DataWriter.writeData(comparsionSheet, "",
						"Problem to assert Response and baseline messages: " + e.getMessage(), ID, test_case);
				DataWriter.writeData(resultSheet, "error", ID, test_case, 0);
				// System.out.println(comparsionSheet.getSheetName() +
				// "\t\tProblem to assert Response and baseline messages:
				// "+e.getMessage()+ "\t\t"+ ID +"\t\t"+ test_case);

				failedcase++;
				System.out.println("failedcase:" + failedcase + "=========================================2");
				Assert.fail("Problem to assert Response and baseline messages: " + e.getMessage());
			}
		else {
			DataWriter.writeData(outputSheet, response.statusLine(), ID, test_case);
			if (baseline_message.equals(response.statusLine())) {
				DataWriter.writeData(resultSheet, "true", ID, test_case, 0);
				if(ID.contains("4")){
					System.out.println("--------------------3");
				}
			} else {
				DataWriter.writeData(comparsionSheet,
						"baseline:" + baseline_message + " /r/n response.statusLine:" + response.statusLine(), ID,
						test_case);
				DataWriter.writeData(resultSheet, "false", ID, test_case, 0);
				failedcase++;
				Assert.assertFalse(true);
			}
		}
	}

	@AfterTest
	public void teardown() {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		endTime = sf.format(new Date());
		DataWriter.writeData(resultSheet, totalcase, failedcase, startTime, endTime);
		// System.out.println(resultSheet.getSheetName() + "\t\t"+ totalcase +
		// "\t\t" + failedcase + "\t\t" + startTime + "\t\t" + endTime);

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			wb.write(fileOutputStream);
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}