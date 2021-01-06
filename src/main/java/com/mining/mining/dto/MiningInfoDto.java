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

	@JSONField(name = "rar-md5")
	public String rarMD5;

}