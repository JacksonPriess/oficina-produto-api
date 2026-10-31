package com.dinoco.oficina.catalogoproduto.infrastructure.config;


import com.dinoco.oficina.catalogoproduto.adapters.controllers.ProdutoControllerClean;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoEventPublisher;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoQueryGateway;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoHandler;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoHandler;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar.DesativarProdutoHandler;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar.DesativarProdutoUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid.BuscarProdutoPorIdHandler;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid.BuscarProdutoPorIdUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo.BuscarProdutoPorTermoHandler;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo.BuscarProdutoPorTermoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ProdutoConfig {

    @Bean
    public CriarProdutoUseCase criarProdutoUseCase(ProdutoCommandGateway produtoCommandGateway,
                                                   ProdutoQueryGateway produtoQueryGateway,
                                                   ProdutoEventPublisher eventPublisher,
                                                   PlatformTransactionManager transactionManager) {
        var handler = new CriarProdutoHandler(produtoCommandGateway, produtoQueryGateway, eventPublisher);
        var transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> handler.executar(command));
    }

    @Bean
    public AtualizarProdutoUseCase atualizarProdutoUseCase(ProdutoCommandGateway commandGateway) {
        return new AtualizarProdutoHandler(commandGateway);
    }

    @Bean
    public DesativarProdutoUseCase desativarProdutoUseCase(ProdutoCommandGateway commandGateway) {
        return new DesativarProdutoHandler(commandGateway);
    }

    @Bean
    public BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase(ProdutoQueryGateway queryGateway) {
        return new BuscarProdutoPorIdHandler(queryGateway);
    }

    @Bean
    public BuscarProdutoPorTermoUseCase buscarProdutoPorTermoUseCase(ProdutoQueryGateway queryGateway) {
        return new BuscarProdutoPorTermoHandler(queryGateway);
    }

    @Bean
    public ProdutoControllerClean produtoControllerClean(
            CriarProdutoUseCase criarProdutoUseCase,
            AtualizarProdutoUseCase atualizarProdutoUseCase,
            DesativarProdutoUseCase desativarProdutoUseCase,
            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
            BuscarProdutoPorTermoUseCase buscarProdutoPorTermoUseCase) {
        return new ProdutoControllerClean(criarProdutoUseCase, atualizarProdutoUseCase, desativarProdutoUseCase, buscarProdutoPorIdUseCase, buscarProdutoPorTermoUseCase);
    }
}
