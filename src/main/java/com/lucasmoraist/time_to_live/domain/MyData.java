package com.lucasmoraist.time_to_live.domain;

import java.util.Map;

public record MyData(
        Object data,
        Map<String, Object> metadata
) {

}
