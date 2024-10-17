package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Metrics implements Serializable {

    private boolean enabled = true;

    private List<MetricType> types = Arrays.asList(MetricType.values());

    private List<MetricTag> tags = new ArrayList<>();

    public Metrics(List<MetricTag> metricTags) {

        metricTags.forEach(tag -> {
            this.tags.add(tag);

            tag.getTypes().forEach(type -> {
                if(!types.contains(type)) {
                    types.add(type);
                }
            });
        });

    }


}
