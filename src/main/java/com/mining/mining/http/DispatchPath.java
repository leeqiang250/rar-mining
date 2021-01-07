package com.mining.mining.http;

public class DispatchPath {

	public static final String MINING_INFO = "/mining-info";
	public static final String MINING_RUN_REPORT = "/mining-run-report?ip=%s&group=%s&key=%s";

	public static final String TASK_DOWNLOAD_RAR_FILE = "/task-download-rar-file";
	public static final String TASK_GET = "/task-get";
	public static final String TASK_CONFIRM = "/task-confirm?group=%s";
	public static final String TASK_COMPLETE = "/task-complete?group=%s";
	public static final String TASK_DISCOVER = "/task-discover?group=%s&key=%s";

}