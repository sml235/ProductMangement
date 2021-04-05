package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {

    private Map<Product, List<Review>> products = new HashMap<>();
    private ResourseFormatter formatter;
    private static Map<String, ResourseFormatter> formatters = Map.of(
            "en-GB", new ResourseFormatter(Locale.UK),
            "en-US", new ResourseFormatter(Locale.US),
            "ru-RU", new ResourseFormatter(new Locale("ru", "RU")),
            "zh-CN", new ResourseFormatter(Locale.CHINA)
    );
    private Logger logger = Logger.getLogger(ProductManager.class.getName());
    private ResourceBundle config = ResourceBundle.getBundle("config");

    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String languageTag) {
        changeLocale(languageTag);
    }

    public void changeLocale(String languageTag) {
        formatter = formatters.getOrDefault(languageTag, formatters.get("ru-RU"));
    }

    public static Set<String> getSupportedFormat() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<Review>());
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<Review>());
        return product;
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return null;
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {
        List<Review> reviews = products.get(product);

        products.remove(product);
        reviews.add(new Review(rating, comments));
//        int sum = 0;
//        for (Review review : reviews) {
//            sum += review.getRating().ordinal();
//        }
//        product = product.applyRating(Math.round((float) sum / reviews.size()));
        product = product.applyRating(
                (int) Math.round(reviews.stream()
                        .mapToInt(x -> x.getRating().ordinal())
                        .average()
                        .orElse(0)));
        this.products.put(product, reviews);
        return product;
    }

    public void printProductReport(Product product) {
        List<Review> reviews = products.get(product);
        StringBuilder txt = new StringBuilder();
        txt.append(formatter.formatProduct(product));
        txt.append("\n");
//        Collections.sort(reviews);
//        for (var review : reviews) {
//            txt.append(formatter.formatReview(review));
//            txt.append("\n");
//        }
//        if (reviews.isEmpty()) {
//            txt.append(formatter.getText("no.reviews"));
//            txt.append("\n");
//        }
        if (reviews.isEmpty()) {
            txt.append(formatter.getText("no.reviews") + "\n");
        } else {
            txt.append(reviews.stream()
                    .map(x -> formatter.formatReview(x))
                    .collect(Collectors.joining("\n")));
        }
        System.out.println(txt);
    }

    public void printProductReport(int id) {
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
//        List<Product> productList = new ArrayList<>(products.keySet());
//        productList.sort(sorter);
        StringBuilder txt = new StringBuilder();
//        for (var product : productList) {
//            txt.append(formatter.formatProduct(product));
//            txt.append("\n");
//        }
        txt.append(
                products.keySet().stream()
                        .filter(filter)
                        .sorted(sorter)
                        .map(x -> formatter.formatProduct(x))
                        .collect(Collectors.joining("\n"))
        );
        System.out.println(txt);
    }

    public Product findProduct(int id) throws ProductManagerException {
//        Product result = null;
//        for (var product : products.keySet()) {
//            if (product.getId() == id) {
//                result = product;
//                break;
//            }
//        }
//        return result;
        return products.keySet()
                .stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ProductManagerException("Product with id " + id + " not found!"));
    }

    public Map<String, String> getDiscounts() {
        return products.keySet().stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.getRating().getStars(),
                                Collectors.collectingAndThen(
                                        Collectors.summingDouble(
                                                product -> product.getDiscount().doubleValue()),
                                        discount -> formatter.moneyFormat.format(discount)
                                )
                        )
                );
    }

    private static class ResourseFormatter {
        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        public ResourseFormatter(Locale locale) {
            this.locale = locale;
            resources = ResourceBundle.getBundle("resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        public String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"),
                    product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));
        }

        public String formatReview(Review review) {
            return MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(),
                    review.getComments());
        }

        public String getText(String key) {
            return resources.getString(key);
        }
    }
}
