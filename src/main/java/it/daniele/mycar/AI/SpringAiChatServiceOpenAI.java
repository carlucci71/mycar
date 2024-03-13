package it.daniele.mycar.AI;

import org.json.JSONObject;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
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
            body.add("file", new UrlResource((String) file));
            body.add("model", "whisper-1");
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/audio/transcriptions", requestEntity, Map.class);
            return response.getBody().get("text").toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


/*

curl --request POST \
  --url https://api.openai.com/v1/audio/transcriptions \
  --header "Authorization: Bearer $OPENAI_API_KEY" \
  --header 'Content-Type: multipart/form-data' \
  --form file=@speech.mp3 \
  --form model=whisper-1


curl https://api.openai.com/v1/audio/speech \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "tts-1-hd",
    "input": "Oggi è una bella giornata di sole",
    "voice": "alloy"
  }' \
  --output speech.mp3

 */
    }

    public InputStream speech(String testo) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.openai.com/v1/audio/speech";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "tts-1");
            requestBody.put("input", testo);
            requestBody.put("response_format", "mp3");
            requestBody.put("voice", "nova");
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
            try {
                var audioFile = new FileOutputStream("response.mp3");
                audioFile.write(response.getBody());
                System.out.println(audioFile.getChannel().size() + " bytes");
                audioFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (OutputStream outputStream = new FileOutputStream("/1/speech.mp3")) {
                byte[] strToBytes = response.getBody();
                outputStream.write(strToBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] data = response.getBody();
            return new ByteArrayInputStream(data);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String chatCompletion(List<Message> listaMessaggi) {
        ChatResponse response = chatClient.call(new Prompt(listaMessaggi));
        return response.getResult().getOutput().getContent();
    }

    public String chatCompletionCallFunction
            (List<Message> listaMessaggi, List<OpenAiApi.FunctionTool> listaTool, List<FunctionCallback> functionCallbacks) {
        OpenAiChatOptions promptOptions = OpenAiChatOptions.builder()
                .withTools(listaTool)
                .withFunctionCallbacks(functionCallbacks) // function code
                .build();

        ChatResponse response = chatClient.call(new Prompt(listaMessaggi, promptOptions));
        return response.getResult().getOutput().getContent();
    }

    public String chatCompletionToolName(List<Message> listaMessaggi, List<OpenAiApi.FunctionTool> listaTool) {
        List<OpenAiApi.ChatCompletionMessage> messages = listaMessaggi
                .stream()
                .map(el -> {
                    ChatCompletionMessage.Role role;
                    switch (el.getMessageType()) {
                        case USER:
                            role = ChatCompletionMessage.Role.USER;
                            break;
                        case SYSTEM:
                            role = ChatCompletionMessage.Role.SYSTEM;
                            break;
                        case ASSISTANT:
                            role = ChatCompletionMessage.Role.ASSISTANT;
                            break;
                        case FUNCTION:
                            role = ChatCompletionMessage.Role.TOOL;
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
