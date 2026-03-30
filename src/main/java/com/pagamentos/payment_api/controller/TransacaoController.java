package com.pagamentos.payment_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pagamentos.payment_api.dto.TransacaoRequest;
import com.pagamentos.payment_api.dto.TransacaoResponse;
import com.pagamentos.payment_api.model.Transacao;
import com.pagamentos.payment_api.service.TransacaoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService service;

    @PostMapping
    public ResponseEntity<TransacaoResponse> criar(@RequestBody @Valid TransacaoRequest request) {
        TransacaoResponse transacao = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponse>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }
}