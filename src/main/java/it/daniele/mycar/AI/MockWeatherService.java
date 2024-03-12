package it.daniele.mycar.AI;

import java.util.function.Function;

public class MockWeatherService implements Function<MockWeatherService.Request, MockWeatherService.Response> {

	public enum Format { celsius, fahrenheit }
	public record Request(String location, Format unit) {}
	public record Response(double temp, Format unit) {}

	public Response apply(Request request) {
		return new Response(30, Format.celsius);
	}
}