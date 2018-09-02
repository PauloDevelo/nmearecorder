package arbutus.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ToolBox {
	public static <T, U, V> T callPrivateMethod(Class<T> returnType, U instance, String methodName, Class<V> paramType, V param) throws Throwable {
		try {
			Method method = instance.getClass().getDeclaredMethod(methodName, paramType);
			method.setAccessible(true);
			
			Object returnObj = method.invoke(instance, param);
			return returnType.cast(returnObj);
			
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException e) {
			throw e;
		}
	}
}
