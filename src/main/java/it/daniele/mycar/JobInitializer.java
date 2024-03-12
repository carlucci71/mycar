package it.daniele.mycar;

import it.daniele.mycar.AI.AggiornaCar;
import it.daniele.mycar.AI.FunctionParameters;
import it.daniele.mycar.AI.FunctionProperties;
import it.daniele.mycar.AI.MockWeatherDayService;
import it.daniele.mycar.AI.MockWeatherService;
import it.daniele.mycar.AI.SpringAiChatServiceOpenAI;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.messages.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobInitializer {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("jobLeggiTelegram")
    private Job job;

    @Autowired
    JobConfig jobConfig;
    @Autowired
    Utility utility;
    @Autowired
    SpringAiChatServiceOpenAI serviceOpenAI;
    @PostConstruct
    public void runJob() {
        try {
            jobLauncher.run(job, new JobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private void testPrevisioniTempo(){
		List<Message> messaggi=List.of(
				new ChatMessage(MessageType.SYSTEM, "Non fare ipotesi sui valori da inserire nelle funzioni. Chiedi chiarimenti se una richiesta dell'utente è ambigua."),
				new ChatMessage(MessageType.USER, "Che tempo farà nei prossimi 5 giorni. Sono a Los Angeles negli USA.")
		);
		FunctionProperties location = new FunctionProperties()
				.setType("string")
				.setEnumString(Arrays.asList(""))
				.setDescription("La città da controllare");

		FunctionProperties format = new FunctionProperties()
				.setType("string")
				.setEnumString(Arrays.asList("celsius", "fahrenheit"))
				.setDescription("L'unità di misura da usare");

		Map<String, FunctionProperties> props = new HashMap<>();
		props.put("location", location);
		props.put("format", format);
		FunctionParameters functionParameters = new FunctionParameters()
				.setType("object")
				.setRequiredPropertyNames(Arrays.asList("location", "format"))
				.setProperties(props);

		FunctionProperties num_days = new FunctionProperties()
				.setType("integer")
				.setEnumString(Arrays.asList(""))
				.setDescription("Il numero di giorni da controllare");

		Map<String, FunctionProperties> propsDayParameter = new HashMap<>();
		propsDayParameter.put("location", location);
		propsDayParameter.put("format", format);
		propsDayParameter.put("num_days", num_days);
		FunctionParameters functionDaysParameters = new FunctionParameters()
				.setType("object")
				.setRequiredPropertyNames(Arrays.asList("location", "format","num_days"))
				.setProperties(propsDayParameter);


		List<OpenAiApi.FunctionTool> listaTools=List.of(
				new OpenAiApi.FunctionTool(OpenAiApi.FunctionTool.Type.FUNCTION,
						new OpenAiApi.FunctionTool.Function("Il tempo di oggi", "tempo_oggi", utility.toJson(functionParameters))
				),
				new OpenAiApi.FunctionTool(OpenAiApi.FunctionTool.Type.FUNCTION,
						new OpenAiApi.FunctionTool.Function("La previsione dei prossimi giorni", "prossimi_giorni_tempo", utility.toJson(functionDaysParameters))
				)
		);

		List functionCallbackWrappers = List.of(
				new FunctionCallbackWrapper<>("tempo_oggi", "Il tempo di oggi",  new MockWeatherService()),
				new FunctionCallbackWrapper<>("prossimi_giorni_tempo", "La previsione dei prossimi giorni",  new MockWeatherDayService())

		);
		//System.out.println(serviceOpenAI.chatCompletion(messaggi));
		//System.out.println(serviceOpenAI.chatCompletionToolName(messaggi, listaTools));//functionCallbackWrappers listaTools
		System.out.println(serviceOpenAI.chatCompletionCallFunction(messaggi, listaTools,functionCallbackWrappers));//functionCallbackWrappers listaTools
	}


}