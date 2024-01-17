package io.github.michelmhl.msavaliadorcredito.application.controllers;

import io.github.michelmhl.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.michelmhl.msavaliadorcredito.application.ex.ErroComunicacaoMicroservicesException;
import io.github.michelmhl.msavaliadorcredito.application.services.AvaliadorCreditoService;
import io.github.michelmhl.msavaliadorcredito.domain.model.DadosAvaliacao;
import io.github.michelmhl.msavaliadorcredito.domain.model.RetornoAvaliacaoCliente;
import io.github.michelmhl.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.michelmhl.msavaliadorcredito.infra.clients.CartoesResourceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacoes-credito")
@RequiredArgsConstructor
public class AvaliadorCreditoController {

    private final AvaliadorCreditoService avaliadorCreditoService;


    @GetMapping
    public String status() {
        return "ok";
    }

    @GetMapping(value = "situacao-cliente", params = "cpf")
    public ResponseEntity consultaSituacaoCliente(@RequestParam("cpf") String cpf)  {
        try {
            SituacaoCliente situacaoCliente = avaliadorCreditoService.obterSituacaoCliente(cpf);
            return ResponseEntity.ok(situacaoCliente);
        }catch (DadosClienteNotFoundException e){
          return ResponseEntity.notFound().build();
        }catch (ErroComunicacaoMicroservicesException e){
            return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
        }
    }

@PostMapping
    public ResponseEntity realizarAvaliacao(@RequestBody DadosAvaliacao dados){
    try {
        RetornoAvaliacaoCliente retornoAvaliacaoCliente = avaliadorCreditoService.realizarAvaliacao(dados.getCpf(), dados.getRenda());
        return ResponseEntity.ok(retornoAvaliacaoCliente);
    }catch (DadosClienteNotFoundException e){
        return ResponseEntity.notFound().build();
    }catch (ErroComunicacaoMicroservicesException e){
        return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
    }
    }
}
