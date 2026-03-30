package com.pagamentos.payment_api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagamentos.payment_api.dto.TransacaoRequest;
import com.pagamentos.payment_api.dto.TransacaoResponse;
import com.pagamentos.payment_api.model.Transacao;
import com.pagamentos.payment_api.model.Transacao.StatusTransacao;
import com.pagamentos.payment_api.repository.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPICO = "transacoes-pendentes";

    public TransacaoResponse criar(TransacaoRequest request) {

        Transacao transacao = Transacao.builder()
                .pagador(request.getPagador())
                .recebedor(request.getRecebedor())
                .valor(request.getValor())
                .status(definirStatus(request.getValor()))
                .criadoEm(LocalDateTime.now())
                .build();

        Transacao salva = repository.save(transacao);

        publicarNoKafka(salva);

        return toResponse(salva);
    }

    private Transacao.StatusTransacao definirStatus(BigDecimal valor) {
        return valor.compareTo(new BigDecimal("10000")) > 0
                ? Transacao.StatusTransacao.SUSPEITA
                : Transacao.StatusTransacao.PENDENTE;
    }

    private void publicarNoKafka(Transacao transacao) {
        try {
            String mensagem = objectMapper.writeValueAsString(toResponse(transacao));
            kafkaTemplate.send(TOPICO, transacao.getId().toString(), mensagem);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar transação", e);
        }
    }

    public List<TransacaoResponse> listarTodas() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TransacaoResponse toResponse(Transacao transacao) {

        return TransacaoResponse.builder()
                .id(transacao.getId())
                .pagador(transacao.getPagador())
                .recebedor(transacao.getRecebedor())
                .valor(transacao.getValor())
                .status(transacao.getStatus().name())
                .criadoEm(transacao.getCriadoEm())
                .build();
    }
}