package com.dinoco.oficina.catalogoproduto.infrastructure.web.mapper;

import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar.DesativarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.dto.ProdutoRequestDto;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.dto.ProdutoResponseDto;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.dto.ProdutoUpdateRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProdutoWebMapper {

    //Converte do DTO da Web para o Input do Core
    CriarProdutoCommand toInput(ProdutoRequestDto dto);

    //Converte do Output do Core para o DTO da Web
    ProdutoResponseDto toResponse(CriarProdutoOutput output);

    //Converte do DTO da Web para o Input do Core
    AtualizarProdutoCommand toAtualizarCommand(Long id, ProdutoUpdateRequestDto dto);

    //Converte do Output do Core para o DTO da Web
    ProdutoResponseDto toAtualizarResponse(AtualizarProdutoOutput output);

    DesativarProdutoCommand toDesativarCommand(Long id);

    ProdutoResponseDto toQueryResponse(ProdutoQueryOutput output);

}