package Core.Commands.OpenAI;

import Core.CommandSystem.ChatUtils;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Debug;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.PostInit;
import com.google.common.base.Splitter;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatGPTChatPrompt
{
	private static OpenAiService SERVICE;
	
	@Debug
	@PostInit
	public static void initApi(){
		String apiKey = Startup.getEnvValue("openai:token");
		SERVICE = new OpenAiService(apiKey, Duration.ofSeconds(60));
	}
	
	@Debug
	@EventListener
	public static void messageEvent(MessageReceivedEvent event){
		if(event.getAuthor().isBot() || event.getAuthor().isSystem()) return;
		
		if(event.getMessage().getMentions().isMentioned(Startup.getClient().getSelfUser())){
			String message = event.getMessage().getContentRaw();
			
			for(User user : event.getMessage().getMentions().getUsers()){
				message = message.replace(user.getAsMention(), user.getIdLong() == Startup.getClient().getSelfUser().getIdLong() ? "" : user.getName());
			}
			
			message = message.strip();
			
			System.out.println("ChatGPT Prompt: " + message);
			
			int maxMessages = 2;
			int maxLength = ((Message.MAX_CONTENT_LENGTH * maxMessages) / 4) - (event.getAuthor().getAsMention().length() / 4);
			
			
			//noinspection SpellCheckingInspection
			CompletionRequest completionRequest = CompletionRequest.builder()
					.prompt(message)
					.model("text-davinci-003")
					.echo(false)
					.temperature(0.25)
					.maxTokens(Math.min(maxLength, 2048))
					.build();
			
			try {
				CompletionResult result = SERVICE.createCompletion(completionRequest);
				result.getChoices().stream().findFirst().ifPresentOrElse(s -> {
					Iterable<String> ms = Splitter.fixedLength(Message.MAX_CONTENT_LENGTH).split(s.getText().strip());
					AtomicInteger num = new AtomicInteger();
					
					ms.forEach(s1 -> {
						ChatUtils.sendMessage(event.getChannel(), (num.get() == 0 ? event.getAuthor().getAsMention() + " " : "") + s1.strip());
						num.getAndIncrement();
					});
					
				}, () -> {
					//No reply found
				});
				
			}catch (Exception e) {
				Logging.exception(e);
			}
		}
	}
	
}