package test;

import org.springframework.jmx.export.assembler.AutodetectCapableMBeanInfoAssembler;
import org.springframework.jmx.export.assembler.MBeanInfoAssembler;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.*;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DynamicMBeanInfoAssembler implements MBeanInfoAssembler, AutodetectCapableMBeanInfoAssembler {

    private MBeanInfoAssembler parentAssembler;

    @Override
    public ModelMBeanInfo getMBeanInfo(final Object managedBean, String beanKey) throws JMException {
        ModelMBeanInfo info = null;
        if (parentAssembler != null) {
            info = parentAssembler.getMBeanInfo(managedBean, beanKey);
        }
        if (managedBean instanceof JmxAttributedMetric) {
            List<ModelMBeanAttributeInfo> attributesInfo = new ArrayList<ModelMBeanAttributeInfo>();
            JmxAttributedMetric metric = (JmxAttributedMetric) managedBean;
            Collection<JmxAttribute> attributes = metric.getAttributes();
            for (final JmxAttribute attribute : attributes) {
                Descriptor descriptor = new AttributedDescriptorSupport(metric, attribute);
                ModelMBeanAttributeInfo attributeInfo = new ModelMBeanAttributeInfo(attribute.getName(),
                        Object.class.getCanonicalName(), attribute.getDescription(), true, false, false, descriptor);
                attributesInfo.add(attributeInfo);
            }
            if (info != null) {
                ModelMBeanAttributeInfo[] attributesCurrent = (ModelMBeanAttributeInfo[]) info.getAttributes();
                attributesInfo.addAll(Arrays.asList(attributesCurrent));
                info = new ModelMBeanInfoSupport(info.getClassName(), info.getDescription(),
                        attributesInfo.toArray(new ModelMBeanAttributeInfo[0]),
                        (ModelMBeanConstructorInfo[]) info.getConstructors(),
                        (ModelMBeanOperationInfo[]) info.getOperations(),
                        (ModelMBeanNotificationInfo[]) info.getNotifications());
            }
            else {
                info = new ModelMBeanInfoSupport(managedBean.getClass().getSimpleName(), null,
                        attributesInfo.toArray(new ModelMBeanAttributeInfo[0]), null, null, null);
            }
        }
        return info;
    }

    public void setParentAssembler(MBeanInfoAssembler parentAssembler) {
        this.parentAssembler = parentAssembler;
    }

    @Override
    public boolean includeBean(Class<?> beanClass, String beanName) {
        if (parentAssembler != null && parentAssembler instanceof AutodetectCapableMBeanInfoAssembler) {
            return ((AutodetectCapableMBeanInfoAssembler) parentAssembler).includeBean(beanClass, beanName);
        }
        return false;
    }

    private static class AttributedDescriptorSupport extends DescriptorSupport {
        private final JmxAttributedMetric metric;
        private final JmxAttribute attribute;

        public AttributedDescriptorSupport(JmxAttributedMetric metric, JmxAttribute attribute) {
            this.metric = metric;
            this.attribute = attribute;
            setField("name", attribute.getName());
            setField("descriptorType", "attribute");
            setField("displayName", attribute.getName());
        }

        @Override
        public synchronized Object getFieldValue(String fieldName) throws RuntimeOperationsException {
            if (fieldName.equals("value")) {
                return metric.getAttributeValue(attribute);
            }
            return super.getFieldValue(fieldName);
        }

        @Override
        public synchronized Object clone() throws RuntimeOperationsException {
            return new AttributedDescriptorSupport(metric, attribute);
        }

        private Object writeReplace() throws ObjectStreamException {
            return new DescriptorSupport(this);
        }

    }

}
