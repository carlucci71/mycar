package it.daniele.mycar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
public class TelegramBot  extends TelegramLongPollingBot {
    @Value("${USERNAME}")
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
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if(update.hasMessage()){
                Long chatId = update.getMessage().getChatId();
                Voice voice = update.getMessage().getVoice();
                if (voice!=null){
                    String fileId = voice.getFileId();
                    GetFile getFileRequest = new GetFile();
                    getFileRequest.setFileId(fileId);
                    try {
                        File file = execute(getFileRequest);
                        String filePath = file.getFilePath();
                        String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;
                        try (InputStream in = new URL(fileUrl).openStream()) {
                            Files.copy(in, Paths.get("/1/voice.ogg"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }                }else {
                    String text = update.getMessage().getText();
                    if(update.getMessage().hasText()){
                        execute(creaSendMessage(chatId,text, true));
                    }
                }
            }
            else if(update.hasCallbackQuery()){
                final AnswerCallbackQuery answer = new AnswerCallbackQuery();
                answer.setShowAlert(false);
                answer.setCallbackQueryId(update.getCallbackQuery().getId());
                answer.setText("OK: " + update.getCallbackQuery().getData());
                Long chatId = update.getCallbackQuery().getMessage().getChatId();
                String testoCallback = update.getCallbackQuery().getData();
                log.info("CALLBACK");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            //			throw new RuntimeException(e);
        }

    }

    private SendMessage creaSendMessage(long chatId, String msg, boolean bReply) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setParseMode("html");
        sendMessage.setChatId(Long.toString(chatId));
        String messaggio="";
        String rep=" ";
        if (bReply) {
            for(int i=0;i<msg.length();i++) {
                rep = rep + "\\u" + Integer.toHexString(msg.charAt(i)).toUpperCase();
            }
            rep=rep+" ";

            rep = rep + " --> ";
            byte[] bytes = msg.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                rep = rep + bytes[i] + ",";
            }
            messaggio="<b>sono il bot reply</b> per  " + chatId;
        }
        messaggio = messaggio + "\n" + msg;
        if(bReply) {
            messaggio = messaggio + "\n" + rep;
        }
        messaggio = messaggio + "\n\n<i>" + " CHI " + "</i>";
        sendMessage.setText(messaggio);
        return sendMessage;
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


}
