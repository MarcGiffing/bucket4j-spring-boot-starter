package com.giffing.bucket4j.spring.boot.starter.context;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Bucket4jConfigurationHolder {

    private List<Bucket4JConfiguration> filterConfiguration = new ArrayList<>();

    public void addFilterConfiguration(Bucket4JConfiguration filterConfiguration) {
        getFilterConfiguration().add(filterConfiguration);
    }

}
