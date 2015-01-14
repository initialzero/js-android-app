package com.jaspersoft.android.jaspermobile.sdk;

import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;
import com.jaspersoft.android.retrofit.sdk.server.DefaultVersionParser;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class DefaultVersionParserTest extends UnitTestSpecification {
    @Test
    public void shouldParseSemanticVersioning() {
        Map<String, Double> doubleMap = new HashMap<String, Double>();
        doubleMap.put("5.0.0", new BigDecimal("5").doubleValue());
        doubleMap.put("5.1.0", new BigDecimal("5.1").doubleValue());
        doubleMap.put("5.2.0", new BigDecimal("5.2").doubleValue());
        doubleMap.put("5.5.0", new BigDecimal("5.5").doubleValue());
        doubleMap.put("5.6.0", new BigDecimal("5.6").doubleValue());
        doubleMap.put("5.6.1", new BigDecimal("5.61").doubleValue());
        doubleMap.put("6.0", new BigDecimal("6").doubleValue());

        for (Map.Entry<String, Double> entry : doubleMap.entrySet()) {
            assertThat(new DefaultVersionParser().parse(entry.getKey()), is(entry.getValue())) ;
        }
    }

    @Test
    public void shouldParseLongSemanticVersioning() {
        Map<String, Double> doubleMap = new HashMap<String, Double>();
        doubleMap.put("5.6.1.2", new BigDecimal("5.612").doubleValue());
        doubleMap.put("5.6.1.2.0", new BigDecimal("5.612").doubleValue());
        doubleMap.put("5.5.6.1.2", new BigDecimal("5.5612").doubleValue());
        doubleMap.put("5.5.6.1.2.0", new BigDecimal("5.5612").doubleValue());
        doubleMap.put("5.5.6.1.2.3", new BigDecimal("5.56123").doubleValue());
        doubleMap.put("5.5.6.1.2.3.0", new BigDecimal("5.56123").doubleValue());

        for (Map.Entry<String, Double> entry : doubleMap.entrySet()) {
            assertThat(new DefaultVersionParser().parse(entry.getKey()), is(entry.getValue())) ;
        }
    }
}
