package com.suixingpay.listeners;

import com.google.gson.Gson;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.util.Calendar;

public class ProgressTrackers implements IInvokedMethodListener {

	private long startTime = 0;
	private int totalExecuted = 0;
	public static int totalRun = 0;

	@Override
	public void afterInvocation(IInvokedMethod invokedMethod,
			ITestResult testResult) {
		if (invokedMethod.isTestMethod()) {
			ITestNGMethod m = invokedMethod.getTestMethod();
			String methodName = m.getConstructorOrMethod().getName();
			String className = m.getTestClass().getRealClass().getSimpleName();
			int status = testResult.getStatus();
			String statusText = "Unknown";
			switch (status) {
			case ITestResult.FAILURE:
				statusText = "Failed";
				break;
			case ITestResult.SUCCESS:
				statusText = "Passed";
				break;
			case ITestResult.SKIP:
				statusText = "Skipped";
				break;
			}
			long elapsedTime = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
			int remainingTestCount = totalRun - totalExecuted;
			long remainingTime = (elapsedTime / totalExecuted)
					* remainingTestCount;
			System.out.println("[Progress]"
					+ formPercentageStr(totalExecuted, totalRun) + " ("
					+ totalExecuted + "/" + totalRun + ") " + ", Elapsed:"
					+ formTimeStr(elapsedTime) + ", Estimated Time Remaining:"
					+ formTimeStr(remainingTime));

			System.out.println("[End] " + methodName + "(" + className + "): "
					+ statusText + "\n");

		}
	}

	@Override
	public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult arg1) {

		if (invokedMethod.isTestMethod()) {
			ITestNGMethod m = invokedMethod.getTestMethod();
			String methodName = m.getConstructorOrMethod().getName();
			String className = m.getTestClass().getRealClass().getSimpleName();
			System.out.println("[Begin] " + methodName + "(" + className + ") ");
			if (startTime == 0) {
				startTime = Calendar.getInstance().getTimeInMillis();
			}
			totalExecuted += 1;
			System.out.println("[测试URL地址]  "+new Gson().toJson(m.getInstance()));
			Object[] objects=arg1.getParameters();
			String [] params=new String[]{"[方法名]  ","[参数名]  ","[参数值] ","[预期值]  "};
			for (int i = 0; i <params.length ; i++) {
				if(params.length<=objects.length){
					System.out.println(params[i]+"\t" + objects[i]);
				}

			}
		}
	}
	private String formPercentageStr(long executedTestCount, long totalTestCount) {
		return Math.round((double) executedTestCount * 100
				/ (double) totalTestCount)
				+ "%";
	}
	private String formTimeStr(long valueInSeconds) {
		long hours = valueInSeconds / 3600;
		valueInSeconds = valueInSeconds % 3600;
		long minutes = valueInSeconds / 60;
		valueInSeconds = valueInSeconds % 60;

		return toTwoDigitsStr(hours) + ":" + toTwoDigitsStr(minutes) + ":"
				+ toTwoDigitsStr(valueInSeconds);
	}
	private String toTwoDigitsStr(long value) {
		if (value < 10) {
			return "0" + value;
		} else {
			return String.valueOf(value);
		}
	}
}
