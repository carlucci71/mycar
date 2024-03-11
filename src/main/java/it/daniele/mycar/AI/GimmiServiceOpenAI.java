package it.daniele.mycar.AI;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("gimmi")
@Service
public class GimmiServiceOpenAI implements IServiceOpenAI{
    @Autowired
    ChatClient chatClient;
    @Override
    public String transcript(String nomeFile) {
        throw new NotImplementedException();
    }

    @Override
    public String chatCompletion(String msgContent) {
        ChatResponse response = chatClient.call(
                new Prompt(
                        msgContent,
                        OpenAiChatOptions.builder()
                                .withModel("gpt-3.5-turbo")
                                .withTemperature(0.4F)
                                .build()
                ));
        return response.getResult().getOutput().getContent();
    }
}
