package test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.management.MalformedObjectNameException;
import java.util.Collection;

@Service
public class MetricsController {

    @Autowired
    private MBeanExporter mBeanExporter;

    @Autowired
    private Collection<JmxAttributedMetric> metrics;

    @Scheduled(fixedRate = 3000)
    public void register() {
        metrics.stream().filter(metric -> metric.isUpdated()).forEach(metric -> {
            try {
                mBeanExporter.unregisterManagedResource(metric.getObjectName());
                mBeanExporter.registerManagedResource(metric, metric.getObjectName());
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        });
    }

}
