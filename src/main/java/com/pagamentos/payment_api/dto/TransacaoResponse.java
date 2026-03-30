package com.pagamentos.payment_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransacaoResponse {

    private Long id;

    private String pagador;

    private String recebedor;

    private BigDecimal valor;

    private String status;
    
    private LocalDateTime criadoEm;
}