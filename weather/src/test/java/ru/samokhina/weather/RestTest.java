package ru.samokhina.weather;

import io.qameta.allure.Description;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.testng.Assert.*;

public class RestTest {
	private static Client client = ClientBuilder.newClient();
	private static URI uri = UriBuilder.fromUri("https://api.weather.yandex.ru/v1/forecast/").build();
	private static String yandexKeyName = "X-Yandex-API-Key";
	private static String yandexKeyValue = "02410c98-a644-4457-bd4e-facca8009bba";

	@Test
	@Description(value = "Тест проверяет код ошибки при попытке доступа к API Яндекс погоды без ключа")
	public void testResponseCodeWithoutAuthorization() {
		Response response = client.target(uri).request().get();
		assertEquals(response.getStatus(), 403);
	}

	@Test
	@Description(value = "Тест проверяет корректный доступ по ключу к API Яндекс погоды")
	public void testCorrectResponseCode() {
		Response response = client.target(uri)
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();
		assertEquals(response.getStatus(), 200);
	}

	@Test
	@Description(value = "Тест проверяет факт получения ответа в виде json")
	public void testObtainingJsonResponse() {
		Response response = client.target(uri)
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();
		String body = response.readEntity(String.class);
		assertNotNull(body);
	}

	@Test
	@Description(value = "Тест проверяет соответствие полученной информации о местоположении " +
			"по корректно заданным координатам для Екатеринбурга")
	public void testCorrectLatAndLonParamsGivingYekaterinburg() {
		Response response = client.target(uri)
				.queryParam("lat","57.153033")
				.queryParam("lon","65.534328")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();
		String jsonResult = response.readEntity(String.class);
		assertTrue(jsonResult.contains("Asia/Yekaterinburg"));
	}

	@Test
	@Description(value = "Тест проверяет, что при некорректно заданных координатах, ответ будет дан для Москвы")
	public void testIncorrectLatAndLonParamsGivingMoscow() {
		Response response = client.target(uri)
				.queryParam("lat","ttt")
				.queryParam("lon","ttt")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();
		String jsonResult = response.readEntity(String.class);
		assertTrue(jsonResult.contains("Europe/Moscow"));
	}

	@Test
	@Description(value = "Тест проверяет, что при незаполненных координатах местоположения, ответ будет дан для Москвы")
	public void testEmptyLatAndLonParamsGivingMoscow() {
		Response response = client.target(uri)
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();
		String jsonResult = response.readEntity(String.class);
		assertTrue(jsonResult.contains("Europe/Moscow"));
	}

	@Test
	@Description(value = "Тест проверяет минимальное количество дней в полученном прогнозе погоды")
	public void testForecastMinDaysCount() {
		Response response = client.target(uri)
				.queryParam("lat","55.75396")
				.queryParam("lon","37.620393")
				.queryParam("limit","2")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();
		String jsonResult = response.readEntity(String.class);

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
		Response response = client.target(uri)
				.queryParam("lat","55.75396")
				.queryParam("lon","37.620393")
				.queryParam("limit","7")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();
		String jsonResult = response.readEntity(String.class);

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
		Response response = client.target(uri)
				.queryParam("lat","55.75396")
				.queryParam("lon","37.620393")
				.queryParam("hours","true")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();

		String jsonResult = response.readEntity(String.class);
		assertEquals(jsonResult.contains("hours"), true);
	}

	@Test
	@Description(value = "Тест проверяет, что почасовой прогноз погоды не будет получен")
	public void testForecastWithoutHours() {
		Response response = client.target(uri)
				.queryParam("lat","55.75396")
				.queryParam("lon","37.620393")
				.queryParam("hours","false")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();

		String jsonResult = response.readEntity(String.class);
		assertEquals(jsonResult.contains("hours"), false);
	}

	@Test
	@Description(value = "Тест проверяет получение подробного прогноза погоды про осадки")
	public void testPrecipitationExtraInfo() {
		Response response = client.target(uri)
				.queryParam("lat","55.75396")
				.queryParam("lon","37.620393")
				.queryParam("extra","true")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();

		String jsonResult = response.readEntity(String.class);
		assertEquals(jsonResult.contains("prec_strength"), true);
	}

	@Test
	@Description(value = "Тест проверяет, что подробный прогноз об осадках не будет получен")
	public void testNonePrecipitationInfo() {
		Response response = client.target(uri)
				.queryParam("lat","55.75396")
				.queryParam("lon","37.620393")
				.queryParam("hours","false")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();

		String jsonResult = response.readEntity(String.class);
		assertEquals(jsonResult.contains("prec_strength"), false);
	}

	@Test
	@Description(value = "Тест проверяет соответствие полученного времени года возможным значениям")
	public void testSeasonValue() {
		String[] validValues = new String[]{"summer", "autumn", "winter", "spring"};

		Response response = client.target(uri)
				.queryParam("lat","55.75396")
				.queryParam("lon","37.620393")
				.queryParam("extra","true")
				.request()
				.header("Content-type", "application/json")
				.header(yandexKeyName, yandexKeyValue)
				.get();

		String jsonResult = response.readEntity(String.class);

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
