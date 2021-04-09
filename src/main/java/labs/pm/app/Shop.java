package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static labs.pm.data.Rating.*;

public class Shop {
    public static void main(String[] args) {
        ProductManager pm = ProductManager.getInstance();
        pm.createProduct(111, "ExtraCake", BigDecimal.valueOf(5.99), FOUR_STAR, LocalDate.now());
        pm.reviewProduct(111, FOUR_STAR, "Nice, yummy cake!");
        pm.reviewProduct(111, FIVE_STAR, "Wow!");
        pm.reviewProduct(111, FIVE_STAR, "Good cake!");
        pm.reviewProduct(111, FIVE_STAR, "Delicious!");

        AtomicInteger clientCount = new AtomicInteger(0);
        Callable<String> client = () -> {
            String clientId = "Client " + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(11) + 101;
            String languageTag = ProductManager.getSupportedFormat()
                    .stream()
                    .skip(ThreadLocalRandom.current().nextInt(4))
                    .findFirst()
                    .get();
            StringBuilder log = new StringBuilder();
            log.append(clientId + " " + threadName + "\n-\tstart of log\t-\n");
            log.append(pm.getDiscounts(languageTag)
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "\t" + entry.getValue())
                    .collect(Collectors.joining("\n")));
            Product product = pm.reviewProduct(productId, FOUR_STAR, "Yet another review");
            log.append((product != null)
                    ? "\nProduct " + productId + " reviewed\n"
                    : "\nProduct " + productId + " not reviewed\n");
            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId + " generated report for " + productId + " product");
            log.append("\n-\tend of log\t-\n");
            return log.toString();
        };

        List<Callable<String>> clients = Stream.generate(()->client).limit(20).collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executorService.invokeAll(clients);
            executorService.shutdown();
            results.stream().forEach((futureResult)->{
                try {
                    System.out.println(futureResult.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Comparator<Product> priceSorter = Comparator.comparing(Product::getPrice);
//        Comparator<Product> ratingSorter = (x,y)->(y.getRating().ordinal()- x.getRating().ordinal());
//        Predicate<Product> filter = x-> x.getPrice().floatValue()>2;
//        pm.printProducts(filter,ratingSorter.thenComparing(priceSorter));
//        pm.getDiscounts().forEach((rating,discount)-> System.out.println(rating+ " "+discount));
    }
}
