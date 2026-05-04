abstract class BaseRequestHandler {
    private BaseRequestHandler nextHandler;

    abstract void handle(Request request);

    public void setNext(BaseRequestHandler handler) {
        this.nextHandler = handler;
    }

    public void forward(Request request) {
        if(nextHandler != null) {
            nextHandler.handle(request);
        }
    }
}

class AuthenticationHandler extends BaseRequestHandler {
    @Override
    public void handle(Request request) {
        if(request.getUser() != null) {
            System.out.println("Authenticated User, forwarding request to Authorization Handler ...");
            forward(request);
        } else {
            System.out.println("Invalid User, terminating request ... ");
        }
    }
}

class AuthorizationHandler extends BaseRequestHandler {
    @Override
    public void handle(Request request) {
        if("admin".equalsIgnoreCase(request.getRole())) {
            System.out.println("Authorized User, forwarding request to Application Handler ...");
            forward(request);
        } else {
            System.out.println("Un-Authorized User, terminating request ... ");
        }
    }
}

class ApplicationHandler extends BaseRequestHandler {
    @Override
    public void handle(Request request) {
        System.out.println("Processing request ...");
        forward(request);
    }
}

public class CRDemo {
    public static void main(String[] args) {
        BaseRequestHandler authentication = new AuthenticationHandler();
        BaseRequestHandler authorization = new AuthorizationHandler();
        BaseRequestHandler application = new ApplicationHandler();

        authentication.setNext(authorization);
        authorization.setNext(application);

        authentication.handle(new Request("dharun", "admin"));
        authentication.handle(new Request("kumar", "guest"));

    }
}

class Request {
    private String user;
    private String role;

    public Request(String user, String role) {
        this.user = user;
        this.role = role;
    }

    public String getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }
}