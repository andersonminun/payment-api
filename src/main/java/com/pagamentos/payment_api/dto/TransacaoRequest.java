package com.pagamentos.payment_api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransacaoRequest {

    @NotBlank(message = "Pagador obrigatório")
    private String pagador;

    @NotBlank(message = "Recebedor obrigatório")
    private String recebedor;

    @NotNull
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;
}