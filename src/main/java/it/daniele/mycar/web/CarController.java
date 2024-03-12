package it.daniele.mycar.web;

import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@RequestMapping(value = "car", produces = "application/json")
public class CarController {

    @PostConstruct
    public void a(){
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("********************");
            System.out.println("********************");
            System.out.println("********************");
            System.out.println(hostAddress);
            System.out.println("********************");
            System.out.println("********************");
            System.out.println("********************");
        } catch (Exception e)
        {

        }

    }
    @GetMapping
    ResponseEntity<String> get(){
        return ResponseEntity.ok("OK");
    }

    @PostMapping
    ResponseEntity<RifornimentoResource> aggiorna(@RequestBody RifornimentoDto rifornimentoDto){
        RifornimentoResource rifornimentoResource = new RifornimentoResource();
        String esito = "OK";
        Float prezzo = rifornimentoDto.getPrezzo();
        Float quantita = rifornimentoDto.getQuantita();
        Float totale = rifornimentoDto.getTotale();
        if (prezzo == null) {
            if (totale==null || quantita==null){
                esito = "KO";
            } else {
                prezzo=totale/quantita;
            }
        }
        if (totale==null){
            if (prezzo==null || quantita==null){
                esito = "KO";
            } else {
                totale=prezzo*quantita;
            }
        }
        if (quantita==null){
            if (totale==null || prezzo==null){
                esito = "KO";
            } else {
                quantita=totale/prezzo;
            }
        }
        rifornimentoResource.setPrezzo(prezzo);
        rifornimentoResource.setQuantita(quantita);
        rifornimentoResource.setTotale(totale);
        rifornimentoResource.setEsito(esito);
        return ResponseEntity.ok(rifornimentoResource);
    }
}
