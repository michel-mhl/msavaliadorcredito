package io.github.michelmhl.msavaliadorcredito.application.services;

import feign.FeignException;
import io.github.michelmhl.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.michelmhl.msavaliadorcredito.application.ex.ErroComunicacaoMicroservicesException;
import io.github.michelmhl.msavaliadorcredito.domain.model.*;
import io.github.michelmhl.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.michelmhl.msavaliadorcredito.infra.clients.ClienteResourceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    private final ClienteResourceClient clienteResourceClient;
    private final CartoesResourceClient cartoesResourceClient;


    public SituacaoCliente obterSituacaoCliente(String cpf) throws DadosClienteNotFoundException, ErroComunicacaoMicroservicesException {
        //obterDadosCliente  - MSCLIENTES
        //obter cartoes do cliente   - MSCARTOES

        //mesmo cenário que bater em um repositório, porem vai bater em infra @feing que comunia com outro service
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosCliente(cpf);
            ResponseEntity<List<CartoesCLiente>> cartoesResponse = cartoesResourceClient.getCartaoByCliente(cpf);

            return SituacaoCliente
                    .builder()
                    .cliente(dadosClienteResponse.getBody())
                    .cartoes(cartoesResponse.getBody())
                    .build();
        } catch (FeignException e) {
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            }
            throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
        }
    }

    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) throws DadosClienteNotFoundException, ErroComunicacaoMicroservicesException {
        try {
            ResponseEntity<DadosCliente> dadosClienteResponse = clienteResourceClient.dadosCliente(cpf);
            ResponseEntity<List<Cartao>> cartoesResponse = cartoesResourceClient.getCartoesRendaAte(renda);

            List<Cartao> cartoes = cartoesResponse.getBody();
            var listaCartoesAprovado = cartoes.stream().map(cartao -> {
                // Pegando o limite basico do cartao para calcular o limite aprovado- utilizando a idade do cliente para fazer o calculo
                BigDecimal limiteBasico = cartao.getLimiteBasico();
                DadosCliente dadosCliente = dadosClienteResponse.getBody();
                BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
                var fator = idadeBD.divide(BigDecimal.valueOf(10));
                BigDecimal limiteAprovado = fator.multiply(limiteBasico);


                CartaoAprovado aprovado = new CartaoAprovado();
                aprovado.setCartao(cartao.getNome());
                aprovado.setBandeira(cartao.getBandeira());
                aprovado.setLimiteAprovado(limiteAprovado);

                return aprovado;

            }).collect(Collectors.toList());

            return new RetornoAvaliacaoCliente(listaCartoesAprovado);
        } catch (FeignException e) {
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status) {
                throw new DadosClienteNotFoundException();
            }
            throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
        }

    }

}
