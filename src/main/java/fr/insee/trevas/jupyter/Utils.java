/* (C)2024 */
package fr.insee.trevas.jupyter;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

	public static class QueryParam {
		private final Map<String, List<String>> params = new LinkedHashMap<>();

		public QueryParam(URI uri) {
			Arrays.stream(Optional.ofNullable(uri.getQuery()).orElse("").split("&"))
					.forEach(
							keyVal -> {
								var kv = keyVal.split("=");
								if (kv.length >= 1) {
									var list =
											params.computeIfAbsent(kv[0], k -> new ArrayList<>());
									if (kv.length == 2) {
										list.add(kv[1]);
									} else {
										list.add("");
									}
								}
							});
		}

		public Optional<List<String>> getValues(String key) {
			return Optional.ofNullable(params.get(key));
		}

		public Optional<String> getValue(String key) {
			return getValues(key).map(strings -> strings.get(0));
		}

		public Map<String, String> flatten() {
			return params.keySet().stream()
					.collect(Collectors.toMap(s -> s, s -> getValue(s).get()));
		}
	}

	public static URI strip(URI uri) {
		try {
			return new URI(
					uri.getScheme(),
					uri.getUserInfo(),
					uri.getHost(),
					uri.getPort(),
					uri.getPath(),
					null,
					null);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parse value to URI. If the value has no scheme, it is interpreted as a local path.
	 *
	 * <p>Ex: /foo/bar becomes file:///foo/bar
	 */
	public static URI uri(String value) {
		try {
			var uri = new URI(value);
			if (uri.getScheme() == null) {
				var tmp = Path.of(uri.getPath()).toUri();
				return new URI(
						tmp
								+ Optional.ofNullable(uri.getQuery())
										.map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
										.map(s -> "?" + s)
										.orElse("")
								+ Optional.ofNullable(uri.getFragment())
										.map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
										.map(s -> "#" + s)
										.orElse(""));
			}
			return uri;
		} catch (URISyntaxException e) {
			return Path.of(value).toUri();
		}
	}
}
