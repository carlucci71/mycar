package it.daniele.mycar.AI;

import cool.cena.openai.OpenAiSource;
import cool.cena.openai.context.OpenAiAudioTranscriptionContext;
import cool.cena.openai.context.OpenAiChatCompletionContext;
import cool.cena.openai.pojo.audio.OpenAiAudioTranscriptionResponseBody;
import cool.cena.openai.pojo.chatcompletion.OpenAiChatCompletionResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("cena")
public class CenaServiceOpenAI implements IServiceOpenAI {

    @Autowired
    OpenAiSource openAiSource;

    public String transcript(String nomeFile){
        OpenAiAudioTranscriptionContext audioTranscription = openAiSource.createAudioTranscriptionContext();
        OpenAiAudioTranscriptionResponseBody response = audioTranscription.create(nomeFile);
        return response.getText();
    }

    public String chatCompletion(String msgContent){
        OpenAiChatCompletionContext chatCompletion = openAiSource.createChatCompletionContext();
        chatCompletion.addUserMessage(msgContent);
        OpenAiChatCompletionResponseBody response = chatCompletion.create();
        return response.getMessage();
    }

}