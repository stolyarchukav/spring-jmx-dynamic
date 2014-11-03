package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final AtomicBoolean stopped = new AtomicBoolean();

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        DynamicMetric metric = context.getBean(DynamicMetric.class);

        ExecutorService executor = updateValues(metric);

        waitForStop();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.exit(1);
    }

    private static void waitForStop() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            if ("q".equals(nextLine) || "exit".equals(nextLine)) {
                stopped.set(true);
                break;
            }
        }
        scanner.close();
    }

    private static ExecutorService updateValues(DynamicMetric metric) {
        int count = 10;
        List<JmxAttribute> attributes = new ArrayList<>(count);
        for (int q = 0; q < count; q++) {
            attributes.add(new JmxAttribute("attr_" + q, "desc_" + q));
        }
        Random random = new Random();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(() -> {
            while (! stopped.get()) {
                JmxAttribute attribute = attributes.get(random.nextInt(count));
                metric.update(attribute, random.nextGaussian());
                sleep(5000);
            }
        });
        executor.execute(() -> {
            while (! stopped.get()) {
                JmxAttribute attribute = attributes.get(random.nextInt(count));
                metric.remove(attribute);
                sleep(8000);
            }
        });
        return executor;
    }

    private static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
