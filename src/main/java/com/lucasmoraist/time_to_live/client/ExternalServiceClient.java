package com.lucasmoraist.time_to_live.client;

import com.lucasmoraist.time_to_live.domain.MyData;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "my-mockoon", url = "http://localhost:8081")
public interface ExternalServiceClient {

    @PostMapping("/process")
    Response processData(@RequestBody MyData data);

}
