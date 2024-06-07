import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {
    private static final String URL_TO_TEST = "http://localhost:8081/files/all"; // 替换为你的目标URL

    public static void main(String[] args) {
        int numberOfThreads = 10; // 你可以根据需要调整线程数量
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < 1000; i++) {
            executor.execute(() -> {
                try {
                    URL url = new URL(URL_TO_TEST);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    System.out.println("Response Code : " + connection.getResponseCode());

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // 这里只输出每次请求的响应代码，以避免在控制台上打印过多信息
                    System.out.println("Response for Thread " + Thread.currentThread().getId() + ": " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            // 等待直到所有任务完成
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

