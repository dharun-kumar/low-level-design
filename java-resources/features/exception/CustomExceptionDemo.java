class APIException extends Exception {
    public APIException(String message) {
        super(message);
    }
}

public class CustomExceptionDemo {

    public static void validateAPIVersion(String url) throws APIException {
        if(!url.contains("v2")) {
            throw new APIException("API Version Not Supported");
        }
    }

    public static void execute(String url) {
        try {
            validateAPIVersion(url);
        } catch (APIException e) {
            System.out.println("Exception Occurred :: " + e.getMessage());
        } finally {
            //set http response as 200 or 404, based on
        }
    }

    public static void main(String[] args) {
        execute("api/v1/search");
    }
}
