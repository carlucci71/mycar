package it.daniele.mycar.AI;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpringAiChatServiceOpenAI {
    @Autowired
    ChatClient chatClient;
    @Autowired
    RestTemplateBuilder restTemplateBuilder;
    @Value("${OPEN_AI_KEY}")
    String OPENAI_API_KEY;
    public String transcript(Object file) {
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new UrlResource((String)file));
            body.add("model", "whisper-1");
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/audio/transcriptions", requestEntity, Map.class);
            return response.getBody().get("text").toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }


/*

curl --request POST \
  --url https://api.openai.com/v1/audio/transcriptions \
  --header "Authorization: Bearer $OPENAI_API_KEY" \
  --header 'Content-Type: multipart/form-data' \
  --form file=@speech.mp3 \
  --form model=whisper-1

 */
    }

    public String chatCompletion(List<Message> listaMessaggi) {
        ChatResponse response = chatClient.call(new Prompt(listaMessaggi));
        return response.getResult().getOutput().getContent();
    }

    public String chatCompletionCallFunction(List<Message> listaMessaggi,List<OpenAiApi.FunctionTool> listaTool,List<FunctionCallback> functionCallbacks){
        OpenAiChatOptions promptOptions = OpenAiChatOptions.builder()
                .withTools(listaTool)
                .withFunctionCallbacks(functionCallbacks) // function code
                .build();

        ChatResponse response = chatClient.call(new Prompt(listaMessaggi, promptOptions));
        return response.getResult().getOutput().getContent();
    }

    public String chatCompletionToolName(List<Message> listaMessaggi,List<OpenAiApi.FunctionTool> listaTool) {
        List<OpenAiApi.ChatCompletionMessage> messages = listaMessaggi
                .stream()
                .map(el -> {
                    ChatCompletionMessage.Role role;
                    switch (el.getMessageType()) {
                        case USER:
                            role=ChatCompletionMessage.Role.USER;
                            break;
                        case SYSTEM:
                            role=ChatCompletionMessage.Role.SYSTEM;
                            break;
                        case ASSISTANT:
                            role=ChatCompletionMessage.Role.ASSISTANT;
                            break;
                        case FUNCTION:
                            role=ChatCompletionMessage.Role.TOOL;
                            break;
                        default:
                            throw new RuntimeException("MessageType non corretto: " + el.getMessageType());
                    }
                    ChatCompletionMessage chatCompletionMessage = new ChatCompletionMessage(el.getContent(), role);
                    return chatCompletionMessage;
                })
                .collect(Collectors.toList());

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(messages, "gpt-4-turbo-preview",
                listaTool, null);

        OpenAiApi completionApi = new OpenAiApi(OPENAI_API_KEY);
        ResponseEntity<OpenAiApi.ChatCompletion> chatCompletion = completionApi.chatCompletionEntity(chatCompletionRequest);
        ChatCompletionMessage message1 = chatCompletion.getBody().choices().get(0).message();
        ChatCompletionMessage.ChatCompletionFunction function = message1.toolCalls().get(0).function();
        return function.name() + "-->" + function.arguments();

    }

}
