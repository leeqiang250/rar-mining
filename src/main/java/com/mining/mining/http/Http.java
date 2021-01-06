package com.mining.mining.http;

import com.alibaba.fastjson.JSONObject;
import com.mining.mining.dto.Dto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Http<T> {

	public static <T> Dto<T> DispatchGet(String url, Class<T> clazz) {
		JSONObject json = JSONObject.parseObject(Get(url), JSONObject.class);
		if (null != json) {
			return new Dto<T>()
					.code(json.getIntValue("code"))
					.msg(json.getString("msg"))
					.ts(json.getLongValue("ts"))
					.data(json.getObject("data", clazz));
		}

		return null;
	}

	public static byte[] Get(String url) {
		CloseableHttpResponse response = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int n = 0;

		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(url);

		try {
			response = client.execute(request);
			if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();

				while (-1 != (n = entity.getContent().read(buffer))) {
					outputStream.write(buffer, 0, n);
				}

				return outputStream.toByteArray();
			}
		} catch (Exception e) {
			e.printStackTrace();
			//
		} finally {
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (null != client) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

}