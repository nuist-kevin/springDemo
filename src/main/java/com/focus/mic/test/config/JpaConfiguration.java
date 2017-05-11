package com.focus.mic.test.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

/**
 * Created by caiwen on 2017/5/10.
 */

@Configuration
@EnableJpaRepositories(basePackages = "com.focus.mic.test.repository")
public class JpaConfiguration {
    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/springdemo");
        dataSource.setUsername("caiwen");
        dataSource.setPassword("caiwen");
        return dataSource;
    }
//
////    @Bean
////    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
////        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
////        sessionFactoryBean.setDataSource(dataSource);
////        sessionFactoryBean.setPackagesToScan("com.focus.mic.test.entity");
////        Properties properties = new Properties();
////        properties.setProperty("dialect", "org.hibernate.dialect.MySQLDialect");
////
////        sessionFactoryBean.setHibernateProperties(properties);
////
////        return sessionFactoryBean;
////    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setGenerateDdl(false);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan("com.focus.mic.test.entity");
        return localContainerEntityManagerFactoryBean;
    }
}
