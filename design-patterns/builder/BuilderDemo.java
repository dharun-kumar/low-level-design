import java.util.Map;
import java.util.HashMap;

class HttpRequest {

    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final Map<String, String> parameters;
    private final int timeout;

    public HttpRequest(HttpRequestBuilder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.parameters = builder.parameters;
        this.timeout = builder.timeout;
    }

    @Override
    public String toString() {
        return String.format("HttpRequest{\n  url='%s',\n  method='%s',\n  headers=%s,\n  parameters=%s,\n  timeout=%d\n}", url, method, headers, parameters, timeout);
    }
}

class HttpRequestBuilder {

    public final String url;
    public String method;
    public Map<String, String> headers = new HashMap<>();;
    public Map<String, String> parameters = new HashMap<>();;
    public int timeout;

    public HttpRequestBuilder(String url) {
        this.url = url;
    }

    public HttpRequestBuilder method(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpRequestBuilder parameter(String key, String value) {
        this.parameters.put(key, value);
        return this;
    }

    public HttpRequestBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public HttpRequest build() {
        return new HttpRequest(this);
    }
}

public class BuilderDemo {
    public static void main(String[] args) {
        HttpRequest getRepo = new HttpRequestBuilder("https://github/api/repo")
                                .method("GET")
                                .header("Content-Type", "application/json")
                                .parameter("user_name", "dharun-kumar")
                                .parameter("repo_name", "kafka-quickstart")
                                .timeout(5000)
                                .build();
        
        System.out.println(getRepo.toString());
    }
}