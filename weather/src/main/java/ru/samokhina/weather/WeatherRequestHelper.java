package ru.samokhina.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class WeatherRequestHelper {

	public String requestWeatherJsonString(String urlString, String type, Map<String, String> requestProperties) {
		String result = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(type);
			if (requestProperties != null) {
				requestProperties.entrySet().forEach(p -> connection.setRequestProperty(p.getKey(), p.getValue()));
			}
			connection.connect();

			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String inputLine;
				final StringBuilder content = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				result = content.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public int requestWeatherResponseCode(String urlString, String type, Map<String, String> requestProperties)
			throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(type);
		if (requestProperties != null) {
			requestProperties.entrySet().forEach(p -> connection.setRequestProperty(p.getKey(), p.getValue()));
		}
		connection.connect();
		return connection.getResponseCode();
	}

	public String requestWeatherResponseMessage(String urlString, String type, Map<String, String> requestProperties)
			throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(type);
		if (requestProperties != null) {
			requestProperties.entrySet().forEach(p -> connection.setRequestProperty(p.getKey(), p.getValue()));
		}
		connection.connect();
		return connection.getResponseMessage();
	}
}
