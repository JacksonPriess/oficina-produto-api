package com.dinoco.oficina.catalogoproduto.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoRabbitConfig {

    public static final String EXCHANGE = "oficina.produtos";
    public static final String ROUTING_KEY = "produto.cadastrado.v1";
    public static final String QUEUE = "nucleo.estoque.produto-cadastrado";

    @Bean
    public Declarables produtoTopology() {
        var exchange = new DirectExchange(EXCHANGE, true, false);

        var queue = QueueBuilder.durable(QUEUE)
                .deadLetterExchange("oficina.produtos.dlx")
                .deadLetterRoutingKey("produto.cadastrado.falhou").build();

        var dlx = new DirectExchange("oficina.produtos.dlx", true, false);

        var dlq = QueueBuilder.durable(QUEUE + ".dlq").build();

        return new Declarables(exchange, queue,
                BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY),
                dlx, dlq, BindingBuilder.bind(dlq).to(dlx).with("produto.cadastrado.falhou"));
    }
}
