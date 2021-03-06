package me.geso.mech2;

import java.io.IOException;
import java.net.URI;

import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main class of the mech2 package. Yet another HTTP client library based on
 * Apache HttpClient.
 * 
 * <pre>
 * 	<code>
 * 	Mech2 mech = Mech2.builder().build();
 *  Mech2Result res = mech.get("/").execute();
 *  System.out.println(res.getResponseBodyAsString());
 * </code>
 * </pre>
 */
public class Mech2 {
	private static Logger logger = LoggerFactory.getLogger(Mech2.class);

	private final HttpClientBuilder httpClientBuilder;
	private final ObjectMapper objectMapper;

	private Mech2(HttpClientBuilder clientBuilder, ObjectMapper objectMapper) {
		this.httpClientBuilder = clientBuilder;
		this.objectMapper = objectMapper;
	}

	/**
	 * Create new GET request object.
	 * 
	 * @param uri
	 * @return
	 */
	public Mech2Request get(URI uri) {
		return new Mech2Request(this, new URIBuilder(uri), new HttpGet());
	}

	/**
	 * Create new POST request object.
	 * 
	 * @param uri
	 * @return
	 * @throws JsonProcessingException
	 */
	public Mech2Request post(URI uri) throws JsonProcessingException {
		return new Mech2Request(this, new URIBuilder(uri), new HttpPost());
	}

	/**
	 * Disable redirect handling.
	 *
	 * @return
	 */
	public Mech2 disableRedirectHandling() {
		this.httpClientBuilder.disableRedirectHandling();
		return this;
	}

	/**
	 * Send HTTP request by HttpUriRequest.
	 *
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public <T> Mech2Result request(HttpUriRequest request) throws IOException {
		long startedOn = System.currentTimeMillis();
		try (CloseableHttpClient client = this.httpClientBuilder.build()) {
			try (CloseableHttpResponse resp = client.execute(request)) {
				logger.info("{}: {}, {} secs", request.getURI(),
						resp.getStatusLine().toString(),
						(System.currentTimeMillis() - startedOn) / 1000.0);
				return new Mech2Result(request, resp, this);
			}
		}
	}

	/**
	 * Get the current HttpClientBuilder object.<br>
	 * You can set parameter for this object.
	 * 
	 * @return
	 */
	public HttpClientBuilder getHttpClientBuilder() {
		return this.httpClientBuilder;
	}

	/**
	 * Get the jackson's ObjectMapper object.<br>
	 * You can configure the parameters.
	 * 
	 * @return
	 */
	public ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	/**
	 * Get the builder object.
	 * 
	 * @return
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Accessors(fluent = true)
	public static class Builder {
		private Builder() {
			this.objectMapper = new ObjectMapper();
			this.objectMapper.configure(
					DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

		@Setter
		private HttpClientBuilder httpClientBuilder = HttpClientBuilder
				.create();

		@Setter
		private ObjectMapper objectMapper;

		public Mech2 build() {
			return new Mech2(this.httpClientBuilder, this.objectMapper);
		}
	}

}
