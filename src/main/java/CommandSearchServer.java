import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CommandSearchServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        server.createContext("/", exchange -> {
            String requestedPath = exchange.getRequestURI().getPath();
            File file = new File("src/main/resources/static", requestedPath.equals("/") ? "index.html" : requestedPath);
            if (file.exists() && !file.isDirectory()) {
                String contentType = Files.probeContentType(file.toPath());
                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, file.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(file.toPath(), os);
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        server.createContext("/directory", exchange -> {
            File file = new File("src/main/resources/static", "directory.html");
            if (file.exists() && !file.isDirectory()) {
                String contentType = Files.probeContentType(file.toPath());
                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, file.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(file.toPath(), os);
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        //serving test malicious .exe file to HTTP
        server.createContext("/testattack", exchange -> {
            File file = new File("src/main/resources/static", "malware.exe");  // Specify the location of your 'malware.exe' file
            if (file.exists() && !file.isDirectory()) {
                String contentType = "application/octet-stream"; // Generic content type for executable files
                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, file.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(file.toPath(), os);
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        server.createContext("/directory/search", new SearchHandlerDirectory());
        server.createContext("/search", new SearchHandler());

        server.setExecutor(null); // default executor
        System.out.println("Server started on port 8080");
        server.start();
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream requestBody = exchange.getRequestBody();
                    String query = new BufferedReader(new InputStreamReader(requestBody))
                            .lines()
                            .collect(Collectors.joining("\n"));
    
                    if (query == null || query.trim().isEmpty()) {
                        String errorResponse = "Error: Empty query.";
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }
    
                    // execute the command and capture the output
                    StringBuilder responseBuilder = new StringBuilder();
                    try {
                        StringBuilder commandBuilder = new StringBuilder();
                        query = query.replaceAll("\\s+", "").toLowerCase();
                        if (query.contains("&")) {
                            int index = query.indexOf("&");
                            query = new StringBuilder(query).insert(index, " /ad /b").toString();
                        } else {
                            query += " /ad /b";
                        }
                        commandBuilder.append("cmd.exe /c dir D:\\BestPlayers\\")
                                .append(query);
                        String command = commandBuilder.toString();
                        System.out.println("Executing command is: " + command);
    
                        Process process = Runtime.getRuntime().exec(command);
    
                        try (BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = stdOutput.readLine()) != null) {
                                responseBuilder.append(line).append("\n");
                            }
                        }
    
                        try (BufferedReader errorOutput = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                            String line;
                            while ((line = errorOutput.readLine()) != null) {
                                responseBuilder.append("Error: ").append(line).append("\n");
                            }
                        }
    
                        process.waitFor(); // wait for the process to finish
                        System.out.println("Command executed successfully.");
                    } catch (Exception e) {
                        responseBuilder.append("Error executing command: ").append(e.getMessage());
                        System.out.println("Error executing command: " + e.getMessage());
                        e.printStackTrace();
                    }
    
                    String response = responseBuilder.toString();
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    System.out.println("Unsupported request method: " + exchange.getRequestMethod());
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                System.out.println("Error in handle method: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    static class SearchHandlerDirectory implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try { 
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream requestBody = exchange.getRequestBody();
                    String query = new BufferedReader(new InputStreamReader(requestBody))
                            .lines()
                            .collect(Collectors.joining("\n"));
    
                    StringBuilder responseBuilder = new StringBuilder();
                    try {
                        StringBuilder commandBuilder = new StringBuilder();
                        commandBuilder.append("cmd.exe /c dir ")
                                .append(query);
                        String command = commandBuilder.toString();
                        System.out.println("Executing command is: " + command);
                        Process process = Runtime.getRuntime().exec(command);
    
                        try (BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = stdOutput.readLine()) != null) {
                                responseBuilder.append(line).append("\n");
                            }
                        }
    
                        try (BufferedReader errorOutput = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                            String line;
                            while ((line = errorOutput.readLine()) != null) {
                                responseBuilder.append("Error: ").append(line).append("\n");
                            }
                        }
    
                        process.waitFor(); // wait for the process to finish
                        System.out.println("Directory search executed successfully.");
                    } catch (Exception e) {
                        responseBuilder.append("Error executing command: ").append(e.getMessage());
                        System.out.println("Error executing command: " + e.getMessage());
                        e.printStackTrace();
                    }
    
                    // Send the response
                    String response = responseBuilder.toString();
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                System.out.println("Error in handle method: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
}
