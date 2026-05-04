import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface QueryParam {
    String value();
}

class UserService {

    public void createUser(@QueryParam("user name") String name, @QueryParam("user age") int age) { }

    public static void main(String[] args) throws NoSuchMethodException {

        Method method = UserService.class.getMethod("createUser", String.class, int.class);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for(int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof QueryParam) {
                    QueryParam param = (QueryParam) annotation;
                    System.out.println(param.value());
                }
            }
        }
    }

}
