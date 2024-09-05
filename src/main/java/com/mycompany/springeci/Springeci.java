/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.springeci;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class Springeci {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, MalformedURLException {
        Class c = Class.forName(args[0]);
        Map<String,Method> services = new HashMap();
        
        if (c.isAnnotationPresent(RestController.class)){
            Method[] methods = c.getDeclaredMethods();
            for (Method m: methods){
                if(m.isAnnotationPresent(GetMapping.class)){
                    String key = m.getAnnotation(GetMapping.class).value();
                    services.put(key,m);
                }
            }
        }

        URL serviceurl = new URL("http://localhost:8080/App/greeting?name=Nicolas");
        String path = serviceurl.getPath();
        String query = serviceurl.getQuery();
        System.out.println("path: " + path);
        System.out.println("query: " + query);


        String serviceName = path.substring(4);
        System.out.println("Service Name: " + serviceName);


        Method ms = services.get(serviceName);
        if (ms != null) {
            Object[] argsToPass = extractArguments(ms, query); // Extraer parámetros de la consulta
            System.out.println("Rta: " + ms.invoke(null, argsToPass));
        } else {
            System.out.println("Not Found: " + serviceName);
        }
    }

    private static Object[] extractArguments(Method method, String query) {
        Map<String, String> queryParams = parseQuery(query);
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam annotation = parameters[i].getAnnotation(RequestParam.class);
                String paramName = annotation.value();
                String value = queryParams.getOrDefault(paramName, annotation.defaultValue());
                args[i] = value;
            }
        }
        return args;
    }


    private static Map<String, String> parseQuery(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length > 1) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }


    
   
    
}
