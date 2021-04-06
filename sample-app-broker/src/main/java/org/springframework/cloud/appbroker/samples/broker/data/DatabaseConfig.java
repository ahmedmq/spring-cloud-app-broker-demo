package org.springframework.cloud.appbroker.samples.broker.data;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import dev.miku.r2dbc.mysql.constant.SslMode;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseConfig {

    final MysqlConfig mysqlConfig;

    public DatabaseConfig(MysqlConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
    }

    @Bean
    MySqlConnectionFactory mySqlConnectionFactory() {

        return MySqlConnectionFactory.from(MySqlConnectionConfiguration.builder()
                .host(mysqlConfig.hostname)
                .database(mysqlConfig.name)
                .user(mysqlConfig.username)
                .password(mysqlConfig.password)
                .sslMode(SslMode.DISABLED)
                .build());
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory mySqlConnectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(mySqlConnectionFactory);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Configuration
    @ConfigurationProperties("vcap.services.mysql.credentials")
    public static class MysqlConfig{
        String hostname;
        String name;
        String username;
        String password;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
