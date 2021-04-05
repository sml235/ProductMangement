package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;

import static labs.pm.data.Rating.*;

public class Shop {
    public static void main(String[] args) {

        ProductManager pm = new ProductManager(Locale.US);
        System.out.println(ProductManager.getSupportedFormat());
        pm.changeLocale("ru-Ru");
        pm.printProductReport(42);
        pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), NOT_RATED);
        pm.reviewProduct(101, FOUR_STAR, "Nice, hot cup of tre!");
        pm.reviewProduct(101, FIVE_STAR, "Wow!");
        pm.reviewProduct(101, THREE_STAR, "Good tea!");
        pm.reviewProduct(101, FIVE_STAR, "Nice cup of tea!");
        pm.createProduct(102, "Coffee", BigDecimal.valueOf(2.99), NOT_RATED);
        pm.reviewProduct(102, THREE_STAR, "NIce, but not tea!");
        pm.reviewProduct(102, TWO_STAR, "Yak!");
        pm.reviewProduct(102, ONE_STAR, "Good try!");
        pm.reviewProduct(102, ONE_STAR, "WTF?!");
        pm.createProduct(103, "Cake", BigDecimal.valueOf(5.99), FOUR_STAR, LocalDate.now());
        pm.reviewProduct(103, FOUR_STAR, "Nice, yummy cake!");
        pm.reviewProduct(103, FIVE_STAR, "Wow!");
        pm.reviewProduct(103, FIVE_STAR, "Good cake!");
        pm.reviewProduct(103, FIVE_STAR, "Delicious!");
        pm.createProduct(104,"Pie",BigDecimal.valueOf(4.0),FIVE_STAR,LocalDate.now().plusDays(3));
        pm.printProductReport(101);
        pm.printProductReport(102);
        pm.printProductReport(103);
        Comparator<Product> priceSorter = (x,y)->(x.getPrice().compareTo(y.getPrice()));
        Comparator<Product> ratingSorter = (x,y)->(y.getRating().ordinal()- x.getRating().ordinal());
        Predicate<Product> filter = x-> x.getPrice().floatValue()>2;
        pm.printProducts(filter,ratingSorter.thenComparing(priceSorter));
        pm.getDiscounts().forEach((rating,discount)-> System.out.println(rating+ " "+discount));
    }
}
