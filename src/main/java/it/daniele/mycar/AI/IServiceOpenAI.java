package it.daniele.mycar.AI;

public interface IServiceOpenAI {
    String transcript(String nomeFile);

    String chatCompletion(String msgContent);

}