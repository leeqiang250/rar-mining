package com.mining.mining.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class MiningInfoDto implements Serializable {

	@JSONField(name = "core-thread-count")
	public int coreThreadCount;

	@JSONField(name = "rar-file-path")
	public String rarFilePath;

	@JSONField(name = "rar-file-path-md5")
	public String rarFilePathMD5;

	@JSONField(name = "program-path")
	public String programPath;

	@JSONField(name = "program-path-md5")
	public String programPathMD5;

	@JSONField(name = "report-interval")
	public int reportInterval;

}