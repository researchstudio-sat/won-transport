package won.transport.taxibot;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import won.bot.framework.bot.utils.BotUtils;

/**
 * Created by fsuda on 28.02.2017.
 */
public class TaxiBotApp {
    public static void main(String[] args) {
        if(!BotUtils.isValidRunConfig()) {
            System.exit(1);
        }
        SpringApplication app = new SpringApplication("classpath:/spring/app/botApp.xml");
        app.setWebEnvironment(false);
        app.run(args);
        // ConfigurableApplicationContext applicationContext =  app.run(args);
        // Thread.sleep(5*60*1000);
        // app.exit(applicationContext);
    }
}
