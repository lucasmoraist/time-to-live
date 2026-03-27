# time-to-live

## Resiliência e Alta Disponibilidade: Fallback Local (In-Memory)

Para garantir que a aplicação continue operando mesmo durante janelas de manutenção ou instabilidades do **Redis**, implementamos uma estratégia de degradação suave (graceful degradation) utilizando mecanismos de cache e filas em memória.

### O Cenário
A aplicação depende do Redis para gerenciar o ciclo de vida de 35 segundos de cada transação. Caso o Redis fique indisponível, a aplicação entra automaticamente em modo de **Resiliência Local**.

### 🛠Mecanismo de Funcionamento
Utilizamos uma combinação de **Circuit Breaker** (via Resilience4j) e um **ScheduledExecutorService** (ou `DelayQueue`) nativo do Java para espelhar o comportamento do Redis localmente.

#### Fluxo de Contingência:
1.  **Detecção de Falha:** O sistema detecta a perda de conexão com o cluster Redis.
2.  **Ativação do Fallback:** As novas requisições de monitoramento são desviadas para um `Map` thread-safe em memória.
3.  **Gerenciamento de Timeout Local:** Uma thread de baixa prioridade (Virtual Thread) monitora os objetos locais. Se o callback não chegar em 35 segundos, o processamento de fallback é disparado pela própria instância da aplicação.
4.  **Recuperação Automática:** Assim que o Redis retorna, o circuito fecha e as novas transações voltam a ser persistidas globalmente.

---

### Prós e Contras da Estratégia

| Recurso | Com Redis (Padrão) | Fallback Local (In-Memory) |
| :--- | :--- | :--- |
| **Disponibilidade** | Alta (Clusterizada) | Crítica (Depende do nó local) |
| **Persistência** | Sim (AOF/RDB em disco) | **Não** (Apenas RAM) |
| **Escalabilidade** | Global (Qualquer pod lê) | Isolada (Apenas o pod que recebeu) |
| **Performance** | Latência de rede | Latência de memória (Ultra rápido) |

---

### Considerações Importantes
* **Risco de Perda de Dados:** Em caso de reinicialização do *Pod* ou da instância durante a queda do Redis, os dados em memória local serão perdidos. Esta estratégia é recomendada para transações onde a **disponibilidade do serviço** é mais prioritária que a persistência absoluta de 100% dos logs de timeout.
* **Consumo de Memória:** Sob carga de 1000 TPS, o uso de memória RAM da aplicação subirá proporcionalmente ao volume de transações retidas durante a janela de 35s. Monitoramento de Heap é essencial.

### Como Ativar/Configurar
No arquivo `application.yaml`, as propriedades de resiliência podem ser ajustadas:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      redisService:
        registerHealthMap: true
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```
