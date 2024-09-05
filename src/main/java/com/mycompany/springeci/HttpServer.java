package com.mycompany.springeci;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class HttpServer {
    private static final String STATIC_FILES_DIR = "src/main/resources";
    private static final Map<String, Method> services = new HashMap<>();
    private static Object serviceInstance;


    public static void main(String[] args) throws Exception {
        initializeServices("com.mycompany.springeci.HelloService");

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server running on port 8080");
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                }
            }
        }
    }


    private static void initializeServices(String className) throws Exception {
        Class<?> c = Class.forName(className);
        if (c.isAnnotationPresent(RestController.class)) {
            serviceInstance = c.getDeclaredConstructor().newInstance();
            Method[] methods = c.getDeclaredMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(GetMapping.class)) {
                    String key = m.getAnnotation(GetMapping.class).value();
                    services.put(key, m);
                }
            }
        }
    }


    private static void handleRequest(Socket clientSocket) throws IOException {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {

            String request = readRequest(inputStream);
            System.out.println("Request: " + request);

            RequestDetails requestDetails = parseRequest(request);

            System.out.println("Request Path: " + requestDetails.path);
            System.out.println("Query Parameters: " + requestDetails.queryParams);

            Method serviceMethod = services.get(requestDetails.path);
            if (serviceMethod != null) {
                invokeServiceMethod(outputStream, serviceMethod, requestDetails.queryParams);
            } else {
                serveStaticFile(outputStream, requestDetails.path);
            }
        }
    }


    private static String readRequest(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        return new String(buffer, 0, bytesRead);
    }


    private static RequestDetails parseRequest(String request) {
        String[] requestParts = request.split(" ");
        if (requestParts.length > 1) {
            String path = requestParts[1];
            if ("/".equals(path)) {
                path = "index.html";
            }
            String queryString = null;
            if (path.contains("?")) {
                int queryIndex = path.indexOf("?");
                queryString = path.substring(queryIndex + 1);
                path = path.substring(0, queryIndex);
            }
            Map<String, String> queryParams = parseQuery(queryString);
            return new RequestDetails(path, queryParams);
        }
        return new RequestDetails("index.html", new HashMap<>());
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }


    private static void invokeServiceMethod(OutputStream outputStream, Method serviceMethod, Map<String, String> queryParams) throws IOException {
        try {
            Object[] methodParams = extractArguments(serviceMethod, queryParams);
            String response = (String) serviceMethod.invoke(serviceInstance, methodParams);
            writeResponse(outputStream, "HTTP/1.1 200 OK", "text/plain", response);
        } catch (Exception e) {
            e.printStackTrace();
            writeResponse(outputStream, "HTTP/1.1 500 Internal Server Error", "text/plain", "Internal Server Error");
        }
    }


    private static void serveStaticFile(OutputStream outputStream, String path) throws IOException {
        String response = HelloService.getStaticFileContent(path);
        writeResponse(outputStream, "HTTP/1.1 200 OK", "text/html", response);
    }


    private static void writeResponse(OutputStream outputStream, String status, String contentType, String content) throws IOException {
        String response = status + "\r\nContent-Type: " + contentType + "\r\nContent-Length: " + content.length() + "\r\n\r\n" + content;
        outputStream.write(response.getBytes());
    }


    private static Object[] extractArguments(Method method, Map<String, String> queryParams) {
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


    private static class RequestDetails {
        String path;
        Map<String, String> queryParams;

        RequestDetails(String path, Map<String, String> queryParams) {
            this.path = path;
            this.queryParams = queryParams;
        }
    }
}