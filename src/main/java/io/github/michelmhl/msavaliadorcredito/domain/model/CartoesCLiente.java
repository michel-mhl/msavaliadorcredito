package io.github.michelmhl.msavaliadorcredito.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartoesCLiente {

    private String nome;
    private String bandeira;
    private BigDecimal limiteLiberado;
}
