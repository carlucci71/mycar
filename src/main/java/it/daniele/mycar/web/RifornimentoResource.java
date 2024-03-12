package it.daniele.mycar.web;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RifornimentoResource {
    String esito;
    Float quantita;
    Float prezzo;
    Float totale;
}
