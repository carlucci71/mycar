package it.daniele.mycar;

import it.daniele.mycar.AI.IServiceOpenAI;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
    IServiceOpenAI serviceOpenAI;
    @PostConstruct
    public void runJob() {
        try {
            jobLauncher.run(job, new JobParameters());
            System.out.println(serviceOpenAI.chatCompletion("che versione sei?"));
            System.out.println(serviceOpenAI.transcript("/1/voice.ogg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}