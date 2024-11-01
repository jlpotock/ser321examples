/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below

The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
*/

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

class WebServer {
  public static void main(String args[]) {
    WebServer server = new WebServer(9000);
  }

  /**
   * Main thread
   * @param port to listen on
   */
  public WebServer(int port) {
    ServerSocket server = null;
    //Socket sock = null;
    // InputStream in = null;
    // OutputStream out = null;

    try {
      server = new ServerSocket(port);
      String serverIP = InetAddress.getLocalHost().getHostAddress();
      System.out.println("Server started at http://" + serverIP + ":" + port);

      while (true) {
        try {
          Socket sock = server.accept();
             System.out.println("Client connected: " + sock.getInetAddress());
             handleClient(sock);
    //         InputStream in = sock.getInputStream();
    //         OutputStream out = sock.getOutputStream();

   //       byte[] response = createResponse(in);
   //       out.write(response);
   //       out.flush();
   //       System.out.println("Response sent to client.");

        } catch (SocketException se) {
          System.err.println("Socket exception: " + se.getMessage());
        } catch (IOException e) {
          System.err.println("Error with client connection: " + e.getMessage());
          e.printStackTrace();
          // out.write("<html>Error processing request</html>".getBytes());
        }
      }


      //  in.close();
      // out.close();
      // sock.close();

    } catch (IOException e) {
      System.err.println("Error handling the client connection: " + e.getMessage());
    } finally {
      if (server != null && !server.isClosed()) {
        try {
          server.close();
          System.out.println("Server socket is closed.");
        } catch (IOException e) {
          System.err.println("Error closing input stream: " + e.getMessage());
        }
      }
    }
  }

private void handleClient(Socket sock) {
  try (InputStream in = sock.getInputStream();
       OutputStream out = sock.getOutputStream()) {

    byte[] response = createResponse(in);
    out.write(response);
    out.flush();
    System.out.println("Response sent to client.");

  } catch (IOException e) {
    System.err.println("Error handling client request: " + e.getMessage());
  } finally {
    try {
      sock.close();
    } catch (IOException e) {
      System.err.println("Error closing client socket: " + e.getMessage());
    }
  }
}

       // if (out != null) {
       //   try {
        //    out.close();
       //   } catch (IOException e) {
       //     System.err.println("Error closing output stream: " + e.getMessage());
       //   }
       // }
       // if (sock != null) {
       //   try {
       //     sock.close();
       //   } catch (IOException e) {
        //    System.err.println("Error closing client socket: " + e.getMessage());
        //  }
       // }
     // }

    //} catch (Exception e){
      //  System.err.println("Could not start the server: " + e.getMessage());
        //e.printStackTrace();

   // } finally {
     // if (server != null) {
     //   try {
     //     server.close();
      //    System.out.println("Server socket is closed.");
      //  } catch (IOException e) {
      //    System.err.println("Error closing server socket: " + e.getMessage());
          // TODO (1) Auto-generated catch block
          //e.printStackTrace();
      //  }
     // }
   // }
 // }

  /**
   * Used in the "/random" endpoint
   */
  private final static HashMap<String, String> _images = new HashMap<>() {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };

  private Random random = new Random();

  /**
   * Reads in socket stream and generates a response
   * @param inStream HTTP input stream from socket
   * @return the byte encoded HTTP response
   */
  public byte[] createResponse(InputStream inStream) {

    byte[] response = null;
    BufferedReader in = null;


    try {

      // Read from socket's input stream. Must use an
      // InputStreamReader to bridge from streams to a reader
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Get header and save the request from the GET line:
      // example GET format: GET /index.html HTTP/1.1

      String request = null;

      boolean done = false;
      while (!done) {
        String line = in.readLine();

        System.out.println("Received: " + line);

        // find end of header("\n\n")
        if (line == null || line.isEmpty())
          done = true;
        // parse GET format ("GET <path> HTTP/1.1")
        else if (line.startsWith("GET")) {
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // extract the request, basically everything after the GET up to HTTP/1.1
          request = line.substring(firstSpace + 2, secondSpace);
        }

      }

      System.out.println("FINISHED PARSING HEADER\n");

      // Generate an appropriate response to the user
      if (request == null) {
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        // create output buffer
        StringBuilder builder = new StringBuilder();
        // NOTE: output from buffer is at the end

        if (request.length() == 0) {
          // shows the default directory page

          // opens the root.html file
          String page = new String(readFileInBytes(new File("www/root.html")));
          // performs a template replacement in the page
          page = page.replace("${links}", buildFileList());

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(page);

        } else if (request.equalsIgnoreCase("json")) {
          // shows the JSON of a random image and sets the header name for that image

          // pick a index from the map
          int index = random.nextInt(_images.size());

          // pull out the information
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: application/json; charset=utf-8\n");
          builder.append("\n");
          builder.append("{");
          builder.append("\"header\":\"").append(header).append("\",");
          builder.append("\"image\":\"").append(url).append("\"");
          builder.append("}");

        } else if (request.equalsIgnoreCase("random")) {
          // opens the random image page

          // open the index.html
          File file = new File("www/index.html");

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(new String(readFileInBytes(file)));

        } else if (request.contains("file/")) {
          // tries to find the specified file and shows it or shows an error

          // take the path and clean it. try to open the file
          File file = new File(request.replace("file/", ""));

          // Generate response
          if (file.exists()) { // success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
          } else { // failure
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("File not found: " + file);
          }
        } else if (request.contains("multiply?")) {

          // This multiplies two numbers, there is NO error handling, so when
          // wrong data is given this just crashes

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          // extract path parameters
          query_pairs = splitQuery(request.replace("multiply?", ""));

          // extract required fields from parameters
          Integer num1 = Integer.parseInt(query_pairs.get("num1"));
          Integer num2 = Integer.parseInt(query_pairs.get("num2"));

          // do math
          Integer result = num1 * num2;

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("Result is: " + result);
          try {
          } catch (NumberFormatException e) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Invalid input, please provide valid numbers for multiplication.");

          } catch (Exception e) {
            builder.append("HTTP/1.1 500 Internal Server Error\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("An unexpected error occurred: " + e.getMessage());
          }

          // TODO: Include error handling here with a correct error code and
          // a response that makes sense


          //Below is my first additional request for WebServer
        } else if (request.contains("weather?")) {
          Map<String, String> query_pairs = splitQuery(request.replace("weather?", ""));
          String location = query_pairs.get("location");
          String unit = query_pairs.get("unit");

          if (location == null || unit == null) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n\n");
            builder.append("<h1>Error: Missing parameters</h1><p>Both 'location' and 'unit' parameters are required.</p>");
          } else if (!unit.equals("C") && !unit.equals("F")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n\n");
            builder.append("<h1>Error: Invalid unit</h1><p>Use 'C' for Celsius or 'F' for Fahrenheit.</p>");

          } else {
            String json = fetchURL("https://api.open-meteo.com/v1/forecast?latitude=35.6895&longitude=139.6917&current_weather=true&temperature_unit=" + unit);
            //JsonParser JSONParser;
            JsonObject jsonResponse = JsonParser.parseString(json).getAsJsonObject();
            String tempCelsius = jsonResponse.getAsJsonObject("current").getAsJsonObject("condition").get("text").getAsString();
            String condition = jsonResponse.getAsJsonObject("current").getAsJsonObject("condition").get("text").getAsString();

            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n\n");
            builder.append("<h1>Weather for ").append(location).append("</h1>");
            builder.append("\n");
            builder.append("<html><body>");
            builder.append("<p>Temperature: ").append(jsonResponse.get("temperature").toString()).append(" ").append(unit).append("</p>");
            builder.append("</body></html>");
          }

          //My second additional request for my WebServer
        } else if (request.contains("agecalc?")) {
          Map<String, String> query_pairs = splitQuery(request.replace("agecalc?", ""));
          String date1 = query_pairs.get("date1");
          String date2 = query_pairs.get("date2");
          String unit = query_pairs.get("unit");

          if (date1 == null || date2 == null || unit == null) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n\n");
            builder.append("<html><body><h1>Error: Missing parameters</h1><p>'date1', 'date2', and 'unit' parameters are required.</p></body></html>");

          } else if (!unit.equals("years") && !unit.equals("days")) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n\n");
            builder.append("<html><body><h1>Error: Invalid unit</h1><p>Use 'years' or 'days' for unit.</p></body></html>");

          } else {
            LocalDate d1 = LocalDate.parse(date1);
            LocalDate d2 = LocalDate.parse(date2);
            long difference = unit.equals("days") ? ChronoUnit.DAYS.between(d1, d2) : ChronoUnit.YEARS.between(d1, d2);

            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n\n");
            builder.append("<h1>Age Difference</h1>");
            builder.append("<p>The difference between ").append(date1).append(" and ").append(date2).append(" is ").append(difference).append(" ").append(unit).append("</p>");

          }

        } else if (request.contains("github?")) {
          // pulls the query from the request and runs it with GitHub's REST API
          // check out https://docs.github.com/rest/reference/
          //
          // HINT: REST is organized by nesting topics. Figure out the biggest one first,
          //     then drill down to what you care about
          // "Owner's repo is named RepoName. Example: find RepoName's contributors" translates to
          //     "/repos/OWNERNAME/REPONAME/contributors"

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          query_pairs = splitQuery(request.replace("github?", ""));

          if (query_pairs == null || !query_pairs.containsKey("query")) {
              builder.append("HTTP/1.1 400 Bad Request\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");
              builder.append("Missing query parameter for GitHub request.");
              response = builder.toString().getBytes();
              return response;

          }
          String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));


          if (json == null) {
            builder.append("HTTP/1.1 500  Internal Server Error\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Failed to fetch the data from GitHub.");
            response = builder.toString().getBytes();
            return response;

          }
           try {
             System.out.println(json);
             JSONArray repository = new JSONArray(json);
             StringBuilder repoDetails = new StringBuilder();

             for (int i = 0; i < repository.length(); i++) {
               JSONObject repo = repository.getJSONObject(i);
               String fullName = repo.getString("full_name");
               int id = repo.getInt("id");
               String ownerLogin = repo.getJSONObject("owner").getString("login");

               repoDetails.append("Repo: ").append(fullName)
                   .append(", ID: ").append(id)
                   .append(", Owner: ").append(ownerLogin)
                   .append("<br>");
             }
             String page = new String(readFileInBytes(new File("www/root.html")));
             page = page.replace("${links}",  buildFileList());
             page = page.replace("${repoDetails}", repoDetails.toString());
           builder.append("HTTP/1.1 200 OK\n");
             builder.append("Content-Type: text/html; charset=utf-8\n");
             builder.append("\n");
             builder.append(repoDetails.toString());
          // TODO: Parse the JSON returned by your fetch and create an appropriate
          // response based on what the assignment document asks for

        } catch (JSONException e) {
             builder.append("HTTP/1.1 500 Internal Server Error\n");
             builder.append("Content-Type: text/html; charset=utf-8\n");
             builder.append("\n");
             builder.append("Error parsing JSON response: " + e.getMessage());
           }
          // if the request is not recognized at all

          builder.append("HTTP/1.1 400 Bad Request\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("Unsupported request" + request);
        }

        // Output
        response = builder.toString().getBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      }
    }

    return response;
  }

  /**
   * Method to read in a query and split it up correctly
   * @param query parameters on path
   * @return Map of all parameters and their specific values
   * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
   */
  public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    // "q=hello+world%2Fme&bob=5"
    String[] pairs = query.split("&");
    // ["q=hello+world%2Fme", "bob=5"]
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
          URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    // {{"q", "hello world/me"}, {"bob","5"}}
    return query_pairs;
  }

  /**
   * Builds an HTML file list from the www directory
   * @return HTML string output of file list
   */
  public static String buildFileList() {
    ArrayList<String> filenames = new ArrayList<>();

    // Creating a File object for directory
    File directoryPath = new File("www/");
    filenames.addAll(Arrays.asList(directoryPath.list()));

    if (filenames.size() > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul>\n");
      for (var filename : filenames) {
        builder.append("<li>" + filename + "</li>");
      }
      builder.append("</ul>\n");
      return builder.toString();
    } else {
      return "No files in directory";
    }
  }

  /**
   * Read bytes from a file and return them in the byte array. We read in blocks
   * of 512 bytes for efficiency.
   */
  public static byte[] readFileInBytes(File f) throws IOException {

    FileInputStream file = new FileInputStream(f);
    ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

    byte buffer[] = new byte[512];
    int numRead = file.read(buffer);
    while (numRead > 0) {
      data.write(buffer, 0, numRead);
      numRead = file.read(buffer);
    }
    file.close();

    byte[] result = data.toByteArray();
    data.close();

    return result;
  }

  /**
   *
   * a method to make a web request. Note that this method will block execution
   * for up to 20 seconds while the request is being satisfied. Better to use a
   * non-blocking request.
   * 
   * @param aUrl the String indicating the query url for the OMDb api search
   * @return the String result of the http request.
   *
   **/
  public String fetchURL(String aUrl) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(aUrl);
      conn = url.openConnection();
      if (conn != null)
        conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
        BufferedReader br = new BufferedReader(in);
        if (br != null) {
          int ch;
          // read the next character until end of reader
          while ((ch = br.read()) != -1) {
            sb.append((char) ch);
          }
          br.close();
        }
      }
      in.close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    return sb.toString();
  }
}
