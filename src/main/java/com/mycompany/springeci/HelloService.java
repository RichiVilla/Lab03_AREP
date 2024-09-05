
package com.mycompany.springeci;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Base64;
import java.util.Locale;

@RestController
public class HelloService{
    private static final String STATIC_FILES_DIR = "src/main/resources";


    @GetMapping("/hello")
    public static String hello(){
        return "Hello World!";
    }

    @GetMapping("/tomorrow")
    public static String tomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String dayOfWeek = tomorrow.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        return "Mañana es " + dayOfWeek;
    }
    
    @GetMapping("/euler")
    public static String euler(){
        return "euler es igual a 2,7182818284590";
    }

    @GetMapping("/editor")
    public static String editor(){
        return "Hola! Richi creó este micro servicio! c:";
    }

    @GetMapping("/greeting")
    public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola, " + name ;
    }

    @GetMapping("/city")
    public static String getCity() {
        return "La ciudad es Bogotá";
    }

    @GetMapping("/goldenratio")
    public static String goldenRatio() {
        return "El número áureo (φ) es aproximadamente 1.6180339887";
    }

    @GetMapping("/currenttime")
    public String currentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "La fecha y hora actual es: " + now.format(formatter);
    }

    @GetMapping("/square")
    public String square(@RequestParam(value = "number", defaultValue = "0") String numberStr) {
        try {
            int number = Integer.parseInt(numberStr);
            int result = number * number;
            return "El cuadrado de " + number + " es: " + result;
        } catch (NumberFormatException e) {
            return "Por favor ingresa un número válido";
        }
    }

    @GetMapping("/sum")
    public String sum(@RequestParam(value = "a", defaultValue = "0") String aStr,
                      @RequestParam(value = "b", defaultValue = "0") String bStr) {
        try {
            int a = Integer.parseInt(aStr);
            int b = Integer.parseInt(bStr);
            int result = a + b;
            return "La suma de " + a + " y " + b + " es: " + result;
        } catch (NumberFormatException e) {
            return "Por favor ingresa números válidos";
        }
    }

    static String getStaticFileContent(String fileName) {
        File file = new File(STATIC_FILES_DIR + File.separator + fileName);

        if (file.exists() && !file.isDirectory()) {
            try {
                String contentType = determineContentType(fileName);
                byte[] fileContent = Files.readAllBytes(file.toPath());

                if (contentType.startsWith("image")) {
                    String base64Image = Base64.getEncoder().encodeToString(fileContent);
                    String htmlResponse = "<!DOCTYPE html>\r\n"
                            + "<html>\r\n"
                            + "    <head>\r\n"
                            + "        <title>Image</title>\r\n"
                            + "    </head>\r\n"
                            + "    <body>\r\n"
                            + "        <center><img src=\"data:" + contentType + ";base64," + base64Image + "\" alt=\"image\"></center>\r\n"
                            + "    </body>\r\n"
                            + "</html>";
                    return "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: " + htmlResponse.length() + "\r\n\r\n" + htmlResponse;
                } else {
                    return "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\nContent-Length: " + fileContent.length + "\r\n\r\n" + new String(fileContent);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "HTTP/1.1 500 Internal Server Error\r\n\r\nError reading file";
            }
        } else {
            return "HTTP/1.1 404 Not Found\r\n\r\nFile not found";
        }
    }


    private static String determineContentType(String fileName) {
        if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}