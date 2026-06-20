package com.aidex.framework.config;

import com.aidex.framework.config.properties.GeneratorProperties;
import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ GeneratorProperties.class })
public class GeneratorConfig
{
    @Bean
    public VelocityEngine velocityEngine()
    {
        Properties properties = new Properties();
        properties.setProperty("resource.loaders", "class");
        properties.setProperty("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty("runtime.strict_mode.enable", "true");
        return new VelocityEngine(properties);
    }
}
