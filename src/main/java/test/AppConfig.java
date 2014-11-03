package test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MBeanInfoAssembler;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ComponentScan(basePackageClasses = AppConfig.class)
@Configuration
public class AppConfig {

    @Bean
    public MBeanExporter mBeanExporter() {
        MBeanExporter mBeanExporter = new MBeanExporter();
        mBeanExporter.setAssembler(mBeanInfoAssembler());
        mBeanExporter.setAutodetect(false);
        return mBeanExporter;
    }

    @Bean
    public MBeanInfoAssembler mBeanInfoAssembler() {
        DynamicMBeanInfoAssembler infoAssembler = new DynamicMBeanInfoAssembler();
        infoAssembler.setParentAssembler(new MetadataMBeanInfoAssembler(new AnnotationJmxAttributeSource()));
        return infoAssembler;
    }

}
