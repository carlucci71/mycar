package it.daniele.mycar.AI;

import it.daniele.mycar.Utility;
import it.daniele.mycar.web.RifornimentoDto;
import it.daniele.mycar.web.RifornimentoResource;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

public class AggiornaCar implements Function<AggiornaCar.Request, AggiornaCar.Response> {

	public record Request(String veicolo, String localita, int km, float quantita, float prezzo, float totale, boolean pieno) {}
	public record Response(String esito, float quantita, float prezzo, float totale) {}

	public Response apply(Request request) {
		try {
			String url = "http://" + Utility.getHostAddress() + ":8718/car";
			RifornimentoDto rif = new RifornimentoDto(request.veicolo, request.localita, request.km, request.quantita, request.prezzo, request.totale, request.pieno);
			RifornimentoResource forObject = new RestTemplate().postForObject(url, rif, RifornimentoResource.class);
			return new AggiornaCar.Response(forObject.getEsito(), forObject.getQuantita(), forObject.getPrezzo(), forObject.getTotale());
		}
		catch (Exception e){
			throw new RuntimeException(e);
		}

	}

}