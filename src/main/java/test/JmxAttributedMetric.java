package test;

import org.springframework.jmx.export.naming.SelfNaming;

import java.util.Collection;

public interface JmxAttributedMetric extends SelfNaming {

    Collection<JmxAttribute> getAttributes();

    Object getAttributeValue(JmxAttribute attribute);

    boolean isUpdated();

}
