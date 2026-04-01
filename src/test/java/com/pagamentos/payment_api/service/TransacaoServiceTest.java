package com.pagamentos.payment_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagamentos.payment_api.dto.TransacaoRequest;
import com.pagamentos.payment_api.dto.TransacaoResponse;
import com.pagamentos.payment_api.model.Transacao;
import com.pagamentos.payment_api.repository.TransacaoRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private TransacaoService service;

    @BeforeEach
    void setUp() {
        Counter counter = mock(Counter.class);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);

    }
    
    @Test
    void deveCriarTransacaoComStatusPendente() {

        // ARRANGE — prepara os dados e comportamento dos mocks
        TransacaoRequest request = new TransacaoRequest();
        request.setPagador("João");
        request.setRecebedor("Maria");
        request.setValor(new BigDecimal("150.00"));

        Transacao transacaoSalva = Transacao.builder()
                .id(1L)
                .pagador("João")
                .recebedor("Maria")
                .valor(new BigDecimal("150.00"))
                .status(Transacao.StatusTransacao.PENDENTE)
                .criadoEm(LocalDateTime.now())
                .build();

        when(repository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        // ACT — executa o que está sendo testado
        TransacaoResponse response = service.criar(request);

        // ASSERT — verifica o resultado
        assertNotNull(response);
        assertEquals("João", response.getPagador());
        assertEquals("Maria", response.getRecebedor());
        assertEquals(new BigDecimal("150.00"), response.getValor());
        assertEquals("PENDENTE", response.getStatus());
    }

    @Test
    void deveSalvarNoRepositoryAoCriarTransacao() {

        // ARRANGE
        TransacaoRequest request = new TransacaoRequest();
        request.setPagador("João");
        request.setRecebedor("Maria");
        request.setValor(new BigDecimal("150.00"));

        Transacao transacaoSalva = Transacao.builder()
                .id(1L)
                .pagador("João")
                .recebedor("Maria")
                .valor(new BigDecimal("150.00"))
                .status(Transacao.StatusTransacao.PENDENTE)
                .criadoEm(LocalDateTime.now())
                .build();

        when(repository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        // ACT
        service.criar(request);

        // ASSERT — verifica que o repository foi chamado exatamente 1 vez
        verify(repository, times(1)).save(any(Transacao.class));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaTransacoes() {

        // ARRANGE
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // ACT
        List<TransacaoResponse> resultado = service.listarTodas();

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveLancarExcecaoQuandoValorForNegativo() {

        TransacaoRequest request = new TransacaoRequest();
        request.setPagador("João");
        request.setRecebedor("Maria");
        request.setValor(new BigDecimal("-50.00"));

        when(repository.save(any(Transacao.class)))
                .thenThrow(new RuntimeException("Valor inválido"));

        assertThrows(RuntimeException.class, () -> service.criar(request));
    }

    @Test
    void deveMarcarComoSuspeitaQuandoValorAcimaDeDezmil() {
        // ARRANGE
        TransacaoRequest request = new TransacaoRequest();
        request.setPagador("João");
        request.setRecebedor("Maria");
        request.setValor(new BigDecimal("10001.00"));

        Transacao transacaoSalva = Transacao.builder()
                .id(1L)
                .pagador("João")
                .recebedor("Maria")
                .valor(new BigDecimal("10001.00"))
                .status(Transacao.StatusTransacao.SUSPEITA)
                .criadoEm(LocalDateTime.now())
                .build();

        when(repository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        // ACT
        TransacaoResponse response = service.criar(request);

        // ASSERT
        assertEquals("SUSPEITA", response.getStatus());
    }

    @Test
    void deveMarcarComoPendenteQuandoValorAbaixoDeDezmil() {
        // ARRANGE
        TransacaoRequest request = new TransacaoRequest();
        request.setPagador("João");
        request.setRecebedor("Maria");
        request.setValor(new BigDecimal("9999.00"));

        Transacao transacaoSalva = Transacao.builder()
                .id(1L)
                .pagador("João")
                .recebedor("Maria")
                .valor(new BigDecimal("9999.00"))
                .status(Transacao.StatusTransacao.PENDENTE)
                .criadoEm(LocalDateTime.now())
                .build();

        when(repository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        // ACT
        TransacaoResponse response = service.criar(request);

        // ASSERT
        assertEquals("PENDENTE", response.getStatus());
    }
}