package ru.samokhina.weather;

import io.qameta.allure.Description;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class YandexWeatherApiTest {
	public static final String ACCESS_KEY = "xxx";

	@Test
	@Description(value = "Тест проверяет корректный доступ по ключу к API Яндекс погоды")
	public void testCorrectResponseCode() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		try {
			int responseCode = requestHelper.requestWeatherResponseCode("https://api.weather.yandex.ru/v1/forecast/",
					"GET", properties);
			assertEquals(responseCode, 200);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет код ошибки при попытке доступа к API Яндекс погоды без ключа")
	public void testResponseCodeWithoutAuthorization() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		try {
			int responseCode = requestHelper.requestWeatherResponseCode(
					"https://api.weather.yandex.ru/v1/forecast/",
					"GET", null);
			assertEquals(responseCode, 403);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет сообщение при попытке доступа к API Яндекс погоды без ключа")
	public void testResponseMessageWithoutAuthorization() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		try {
			String responseMessage = requestHelper.requestWeatherResponseMessage(
					"https://api.weather.yandex.ru/v1/forecast/",
					"GET", null);
			assertEquals(responseMessage, "Forbidden");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет факт получения ответа в виде json")
	public void testObtainingJsonResponse() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&extra=false",
				"GET", properties);
		assertNotNull(jsonResult);
	}

	@Test
	@Description(value = "Тест проверяет соответствие полученной информации о местоположении" +
			"по корректно заданным координатам для Екатеринбурга")
	public void testCorrectLatAndLonParamsGivingYekaterinburg() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=57.153033&lon=65.534328&extra=true",
				"GET", properties);

		try {
			Object obj = new JSONParser().parse(jsonResult);
			JSONObject jo = (JSONObject) obj;
			JSONObject info = (JSONObject) jo.get("info");
			JSONObject tzinfo = (JSONObject) info.get("tzinfo");
			String name = (String) tzinfo.get("name");
			assertEquals(name, "Asia/Yekaterinburg");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет, что при некорректно заданных координатах, ответ будет дан для Москвы")
	public void testIncorrectLatAndLonParamsGivingMoscow() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=ttt&lon=ttt&extra=true",
				"GET", properties);

		try {
			Object obj = new JSONParser().parse(jsonResult);
			JSONObject jo = (JSONObject) obj;
			JSONObject info = (JSONObject) jo.get("info");
			JSONObject tzinfo = (JSONObject) info.get("tzinfo");
			String name = (String) tzinfo.get("name");
			assertEquals(name, "Europe/Moscow");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет, что при незаполненных координатах местоположения, ответ будет дан для Москвы")
	public void testEmptyLatAndLonParamsGivingMoscow() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?extra=true",
				"GET", properties);

		try {
			Object obj = new JSONParser().parse(jsonResult);
			JSONObject jo = (JSONObject) obj;
			JSONObject info = (JSONObject) jo.get("info");
			JSONObject tzinfo = (JSONObject) info.get("tzinfo");
			String name = (String) tzinfo.get("name");
			assertEquals(name, "Europe/Moscow");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет минимальное количество дней в полученном прогнозе погоды")
	public void testForecastMinDaysCount() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&limit=2",
				"GET", properties);

		try {
			Object obj = new JSONParser().parse(jsonResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray forecast = (JSONArray) jo.get("forecasts");
			assertEquals(forecast.size(), 2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет максимальное количество дней в полученном прогнозе погоды")
	public void testForecastMaxDaysCount() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&limit=7",
				"GET", properties);

		try {
			Object obj = new JSONParser().parse(jsonResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray forecast = (JSONArray) jo.get("forecasts");
			assertEquals(forecast.size(), 7);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Description(value = "Тест проверяет получение почасового прогноза погоды")
	public void testForecastHasHours() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&hours=true",
				"GET", properties);
		assertEquals(jsonResult.contains("hours"), true);
	}

	@Test
	@Description(value = "Тест проверяет, что почасовой прогноз погоды не был получен")
	public void testForecastWithoutHours() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&hours=false",
				"GET", properties);
		assertEquals(jsonResult.contains("hours"), false);
	}

	@Test
	@Description(value = "Тест проверяет получение подробного прогноза погоды про осадки")
	public void testPrecipitationExtraInfo() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&extra=true",
				"GET", properties);
		assertEquals(jsonResult.contains("prec_strength"), true);
	}

	@Test
	@Description(value = "Тест проверяет, что подробный прогноз об осадках не был получен")
	public void testNonePrecipitationInfo() {
		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&extra=false",
				"GET", properties);
		assertEquals(jsonResult.contains("prec_strength"), false);
	}

	@Test
	@Description(value = "Тест проверяет соответствие полученного времени года возможным значениям")
	public void testSeasonValue() {
		String[] validValues = new String[]{"summer", "autumn", "winter", "spring"};

		WeatherRequestHelper requestHelper = new WeatherRequestHelper();
		Map<String, String> properties = new HashMap<>();
		properties.put("X-Yandex-API-Key", ACCESS_KEY);
		properties.put("Content-Type", "application/json");
		String jsonResult = requestHelper.requestWeatherJsonString(
				"https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393&limit=2",
				"GET", properties);

		try {
			Object obj = new JSONParser().parse(jsonResult);
			JSONObject jo = (JSONObject) obj;
			JSONObject fact = (JSONObject) jo.get("fact");
			String season = (String) fact.get("season");
			boolean result = false;
			for (int i = 0; i < validValues.length; i++) {
				if(validValues[i].equals(season)){
					result = true;
					break;
				}
			}
			assertEquals(result, true);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
