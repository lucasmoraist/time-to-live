package com.lucasmoraist.time_to_live.service;

import com.lucasmoraist.time_to_live.client.ExternalServiceClient;
import com.lucasmoraist.time_to_live.domain.MyData;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ControlService{

    private final QueueManagerService queueManagerService;
    private final ExternalServiceClient externalServiceClient;

    public ControlService(QueueManagerService queueManagerService, ExternalServiceClient externalServiceClient) {
        this.queueManagerService = queueManagerService;
        this.externalServiceClient = externalServiceClient;
    }

    @Async
    public void saveData(MyData data) {
        String transactionId = UUID.randomUUID().toString();
        Map<String, Object> metadata = Map.of("transactionId", transactionId);

        try (Response response = this.externalServiceClient.processData(new MyData(
                data.data(),
                metadata
        ))) {
            if (response.status() == 204) {
                log.info("[{}] - Chamada enviada com sucesso", transactionId);
                queueManagerService.monitorar(transactionId, data);
            } else {
                log.warn("[{}] - Chamada falhou com status {}", transactionId, response.status());
            }
        }
    }

    @Async
    public void interruptMonitoring(UUID transactionId) {
        log.info("[{}] - Interrompendo monitoramento", transactionId);
        queueManagerService.interromperMonitoramento(transactionId.toString());
    }

}
