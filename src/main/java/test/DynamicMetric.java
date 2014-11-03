package test;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ManagedResource
@Service
public class DynamicMetric implements JmxAttributedMetric {

    private final Map<JmxAttribute, Object> attributes = new ConcurrentHashMap<>();

    private final AtomicBoolean updated = new AtomicBoolean(true);

    public DynamicMetric() {
        attributes.put(new JmxAttribute("firstAttr", "The first default attribute"), "No value");
    }

    @Override
    public Collection<JmxAttribute> getAttributes() {
        return attributes.keySet();
    }

    @Override
    public Object getAttributeValue(JmxAttribute attribute) {
        return attributes.get(attribute);
    }

    @Override
    public boolean isUpdated() {
        return updated.getAndSet(false);
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("metric:name=dynamic");
    }

    public void update(JmxAttribute attribute, Object value) {
        attributes.put(attribute, value);
        updated.set(true);
    }

    public void remove(JmxAttribute attribute) {
        attributes.remove(attribute);
    }

}
