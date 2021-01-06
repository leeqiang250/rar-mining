package com.mining.mining.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class TaskDto implements Serializable {

	public String group;
	public String text;

}