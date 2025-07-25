package assertions;

import org.junit.jupiter.api.Assertions;

import java.net.http.HttpResponse;

public class HttpResponseAssertions {
    public static void assertOk(HttpResponse<String> response) {
        Assertions.assertEquals(200, response.statusCode());
    }

    public static void assertCreated(HttpResponse<String> response) {
        Assertions.assertEquals(201, response.statusCode());
    }

    public static void assertNotAcceptable(HttpResponse<String> response) {
        Assertions.assertEquals(406, response.statusCode());
    }

    public static void assertNotFound(HttpResponse<String> response) {
        Assertions.assertEquals(404, response.statusCode());
    }
}
