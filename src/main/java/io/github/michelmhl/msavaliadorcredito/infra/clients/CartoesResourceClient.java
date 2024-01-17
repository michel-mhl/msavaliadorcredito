package io.github.michelmhl.msavaliadorcredito.infra.clients;

import io.github.michelmhl.msavaliadorcredito.domain.model.Cartao;
import io.github.michelmhl.msavaliadorcredito.domain.model.CartoesCLiente;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "mscartoes", path = "/cartoes")
public interface CartoesResourceClient {

    @GetMapping(params = "cpf")
    ResponseEntity<List<CartoesCLiente>> getCartaoByCliente(@RequestParam("cpf") String cpf);


    @GetMapping(params = "renda")
    ResponseEntity<List<Cartao>> getCartoesRendaAte(@RequestParam("renda") Long renda);
}

