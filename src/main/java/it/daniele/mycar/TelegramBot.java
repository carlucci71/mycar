package it.daniele.mycar;


import it.daniele.mycar.AI.AggiornaCar;
import it.daniele.mycar.AI.FunctionParameters;
import it.daniele.mycar.AI.FunctionProperties;
import it.daniele.mycar.AI.SpringAiChatServiceOpenAI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${BOT_USERNAME}")
    private String BOT_USERNAME;

    @Value("${BOT_TOKEN}")
    private String BOT_TOKEN;

    @Value("${MY_CHAT_ID}")
    private String MY_CHAT_ID;

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Autowired
    SpringAiChatServiceOpenAI serviceOpenAI;

    @Autowired
    Utility utility;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Utility.getHostAddress();
                Long chatId = update.getMessage().getChatId();
                Voice voice = update.getMessage().getVoice();
                String transcript=null;
                if (voice != null) {
                    String fileId = voice.getFileId();
                    GetFile getFileRequest = new GetFile();
                    getFileRequest.setFileId(fileId);
                    try {
                        File file = execute(getFileRequest);
                        String filePath = file.getFilePath();
                        String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;
                        transcript = serviceOpenAI.transcript(fileUrl);
                        execute(creaSendMessage(chatId, transcript));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    transcript = update.getMessage().getText();
                    if (update.getMessage().hasText()) {
                        execute(creaSendMessage(chatId, transcript));
                    }
                }
                String elab=testAggiornaCar(transcript);
                InputStream speech = serviceOpenAI.speech(elab);
                inviaVocale(chatId, speech);

                execute(creaSendMessage(chatId, elab));
            } else if (update.hasCallbackQuery()) {
                final AnswerCallbackQuery answer = new AnswerCallbackQuery();
                answer.setShowAlert(false);
                answer.setCallbackQueryId(update.getCallbackQuery().getId());
                answer.setText("OK: " + update.getCallbackQuery().getData());
                Long chatId = update.getCallbackQuery().getMessage().getChatId();
                String testoCallback = update.getCallbackQuery().getData();
                log.info("CALLBACK");
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            //			throw new RuntimeException(e);
        }

    }

    private SendMessage creaSendMessage(long chatId, String msg) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setParseMode("html");
        sendMessage.setChatId(Long.toString(chatId));
        String messaggio = "";
        String rep = " ";
        messaggio = messaggio + "\n" + msg;
        sendMessage.setText(messaggio);
        return sendMessage;
    }

    public void inviaVocale(Long chatId, InputStream fileAudio) {
        SendVoice sendVoiceRequest = new SendVoice();
        sendVoiceRequest.setChatId(chatId);
        InputFile inputFile = new InputFile(fileAudio, "tmp.mp3");
        sendVoiceRequest.setVoice(inputFile);
        try {
            execute(sendVoiceRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    BotSession registerBot;

    public void startBot() {
        registerBot.start();
    }

    public void stopBot() {
        registerBot.stop();
    }

    public boolean isRunning() {
        return registerBot.isRunning();
    }

    private String testAggiornaCar(String testo){
        List<Message> messaggi=List.of(
                new ChatMessage(MessageType.SYSTEM, "Non fare ipotesi sui valori da inserire nelle funzioni. Chiedi chiarimenti se una richiesta dell'utente è ambigua."),
                new ChatMessage(MessageType.USER, testo)
        );
        Map<String, FunctionProperties> props = new HashMap<>();
        FunctionProperties veicolo = new FunctionProperties()//
                .setType("string")
                .setEnumString(Arrays.asList(""))
                .setDescription("Il veicolo da aggiornare");
        props.put("veicolo", veicolo);
        FunctionProperties data = new FunctionProperties()
                .setType("date")
                .setEnumString(Arrays.asList(""))
                .setDescription("Il giorno in cui hai fatto rifornimento");
        props.put("data", data);//
        FunctionProperties localita = new FunctionProperties()
                .setType("string")
                .setEnumString(Arrays.asList(""))
                .setDescription("La località in cui hai fatto rifornimento");
        props.put("localita", localita);//
        FunctionProperties km = new FunctionProperties()
                .setType("integer")
                .setEnumString(Arrays.asList(""))
                .setDescription("I km che aveva la macchina quando hai fatto rifornimento");
        props.put("km", km);
        FunctionProperties quantita = new FunctionProperties()
                .setType("double")
                .setEnumString(Arrays.asList(""))
                .setDescription("La quantità di rifornimento fatta");
        props.put("quantita", quantita);
        FunctionProperties prezzo = new FunctionProperties()
                .setType("double")
                .setEnumString(Arrays.asList(""))
                .setDescription("Il prezzo della benzina");
        props.put("prezzo", prezzo);
        FunctionProperties totale = new FunctionProperties()
                .setType("double")
                .setEnumString(Arrays.asList(""))
                .setDescription("Il costo totale del rifornimento");
        props.put("totale", totale);
        FunctionProperties pieno = new FunctionProperties()
                .setType("boolean")
                .setEnumString(Arrays.asList(""))
                .setDescription("Se hai fatto il pieno oppure no");
        props.put("pieno", pieno);
        FunctionParameters functionParameters = new FunctionParameters()
                .setType("object")
                .setRequiredPropertyNames(Arrays.asList("veicolo","data","localita","km","quantita","prezzo", "totale", "pieno"))
                .setProperties(props);

        List<OpenAiApi.FunctionTool> listaTools=List.of(
                new OpenAiApi.FunctionTool(OpenAiApi.FunctionTool.Type.FUNCTION,
                        new OpenAiApi.FunctionTool.Function("Aggiorna la mia auto", "aggiorna_car", utility.toJson(functionParameters))
                )
        );

        List functionCallbackWrappers = List.of(
                new FunctionCallbackWrapper<>("aggiorna_car", "Aggiorna la mia auto",  new AggiornaCar())

        );
        return serviceOpenAI.chatCompletionCallFunction(messaggi, listaTools,functionCallbackWrappers);
    }

}
