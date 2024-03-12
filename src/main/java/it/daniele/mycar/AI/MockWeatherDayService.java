package it.daniele.mycar.AI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MockWeatherDayService implements Function<MockWeatherDayService.Request, MockWeatherDayService.Response> {

	public enum Format { celsius, fahrenheit }
	public record Request(String location, Format unit, Integer num_days) {}
	public record Response(double temp, Format unit, int days) {}

	public Response apply(Request request) {
		String url = "http://85.235.148.177:8080/fantalive/test?conLive=true";
		//gimmi(url);
		Map<String, Object> forObject = new RestTemplate().getForObject(url, Map.class);
		return new Response((Integer) ((Map)((List)((Map)forObject.get("BE")).get("squadre")).get(3)).get("prog"), Format.celsius, request.num_days);
	}
	private void gimmi(String url) {
		try {
			SslContext sslContext = SslContextBuilder
					.forClient()
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();

			reactor.netty.http.client.HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
			WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
			/*
			Flux<Map<String, Object>> response = webClient.get()
					.uri(url)
					.retrieve()
					.bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {});

			List<Map<String, Object>> responseBody = response.collectList().block();
			System.out.println(responseBody);
			 */
			Mono<Map<String, Object>> response = webClient.get()
					.uri(url)
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});

			Map<String, Object> responseBody = response.block();
			System.out.println(responseBody);

		} catch (SSLException e) {
			e.printStackTrace();
		}
	}

}