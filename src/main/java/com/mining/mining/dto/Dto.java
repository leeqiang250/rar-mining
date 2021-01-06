package com.mining.mining.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Dto<T> implements Serializable {

	public int code;
	public String msg;
	public long ts;
	public T data;

	public static boolean success(Dto dto) {
		return null != dto && dto.code == 0;
	}

}