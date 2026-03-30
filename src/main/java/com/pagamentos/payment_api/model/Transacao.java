package com.pagamentos.payment_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pagador;

    private String recebedor;
    
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusTransacao status;

    private LocalDateTime criadoEm;

    public enum StatusTransacao {
        PENDENTE, APROVADA, REJEITADA, SUSPEITA
    }
}
