package com.dinoco.oficina.catalogoproduto.infrastructure.web;

import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo.BuscarProdutoPorTermoQuery;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.dto.ProdutoRequestDto;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.dto.ProdutoResponseDto;
import com.dinoco.oficina.catalogoproduto.adapters.controllers.ProdutoControllerClean;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.dto.ProdutoUpdateRequestDto;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.mapper.ProdutoWebMapper;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar.DesativarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid.BuscarProdutoPorIdQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@Tag(name = "Produtos", description = "Catalogo de produtos")
@RestController
@RequestMapping("api/produtos")

public class ProdutoSpringWebController {

    private final ProdutoControllerClean controllerClean;
    private final ProdutoWebMapper mapper;

    public ProdutoSpringWebController(ProdutoControllerClean controllerClean, ProdutoWebMapper mapper) {
        this.controllerClean = controllerClean;
        this.mapper = mapper;
    }

    @Operation(summary = "Incluir produto")
    @PostMapping
    public ResponseEntity<ProdutoResponseDto> cadastrar(@RequestBody @Valid ProdutoRequestDto request) {
        CriarProdutoCommand input = mapper.toInput(request);
        CriarProdutoOutput output = controllerClean.criarProduto(input);
        ProdutoResponseDto response = mapper.toResponse(output);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Atualizar produto")
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDto> atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoUpdateRequestDto requestUpdate) {
        AtualizarProdutoCommand command = mapper.toAtualizarCommand(id, requestUpdate);
        AtualizarProdutoOutput output = controllerClean.atualizarProduto(command);
        ProdutoResponseDto response = mapper.toAtualizarResponse(output);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Desativar produto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        DesativarProdutoCommand command = mapper.toDesativarCommand(id);
        controllerClean.desativarProduto(command);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar produto por código")
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDto> buscarPorId(@PathVariable Long id) {
        BuscarProdutoPorIdQuery query = new BuscarProdutoPorIdQuery(id);
        ProdutoQueryOutput catalogoProdutos = controllerClean.buscarPorId(query);
        ProdutoResponseDto response = mapper.toQueryResponse(catalogoProdutos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar produto por termo")
    @GetMapping
    public ResponseEntity<List<ProdutoResponseDto>> buscarPorTermo(@RequestParam(value = "busca", required = false) String termo) {
        BuscarProdutoPorTermoQuery query = new BuscarProdutoPorTermoQuery(termo);
        List<ProdutoQueryOutput> catalogoProdutos = controllerClean.buscarPorTermo(query);
        List<ProdutoResponseDto> response = catalogoProdutos.stream()
                .map(mapper::toQueryResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}