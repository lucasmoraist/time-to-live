package com.lucasmoraist.time_to_live.service;

import com.lucasmoraist.time_to_live.domain.MyData;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class QueueManagerService {

    private final RDelayedQueue<String> delayedQueue;
    private final RBlockingQueue<String> blockingQueue;
    private final RedissonClient redisson;

    private final ExecutorService fallbackExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public QueueManagerService(RedissonClient redisson) {
        this.redisson = redisson;
        this.blockingQueue = redisson.getBlockingQueue("fallback_queue");
        this.delayedQueue = redisson.getDelayedQueue(blockingQueue);
    }

    @PostConstruct
    public void init() {
        startFallbackWorker();
    }

    public void monitorar(String transactionId, MyData data) {
        // 1. Salva o objeto no Redis (sem TTL para o fallback poder ler)
        redisson.getBucket("data:" + transactionId).set(data, 120, TimeUnit.SECONDS); // TTL de 120s para evitar acúmulo de dados antigos

        // 2. Agenda o ID na fila atrasada por 35 segundos
        delayedQueue.offer(transactionId, 35, TimeUnit.SECONDS);
    }

    public void interromperMonitoramento(String transactionId) {
        // Se o callback chegou, removemos da fila e deletamos os dados
        delayedQueue.remove(transactionId);
        redisson.getBucket("data:" + transactionId).delete();
    }

    private void startFallbackWorker() {
        Thread.ofVirtual().name("fallback-consumer").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // O take() bloqueia aqui até surgir um ID
                    String id = blockingQueue.take();

                    // Dispara o processamento em uma NOVA Virtual Thread e volta para o take() imediatamente
                    fallbackExecutor.submit(() -> executarFallback(id));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Erro ao buscar da fila", e);
                }
            }
        });
    }

    private void executarFallback(String id) {
        RBucket<MyData> bucket = redisson.getBucket("data:" + id);
        MyData data = bucket.get();
        if (data != null) {
            log.warn("TIMEOUT: Processando fallback para a transação {}", id);
            // Lógica de fallback aqui...
            log.info("Fallback executado para a transação {}: {}", id, data);
            bucket.delete();
        }
    }

}
