package it.daniele.mycar.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RifornimentoDto {
    String veicolo;
    String localita;
    Integer km;
    Float quantita;
    Float prezzo;
    Float totale;
    Boolean pieno;

}
