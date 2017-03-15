package won.transport.taxi.bot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by fsuda on 28.02.2017.
 */
public class TaxiBotApp {
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(
                new Object[]{"classpath:/spring/app/taxiBotApp.xml"}
        );
        app.setWebEnvironment(false);
        ConfigurableApplicationContext applicationContext =  app.run(args);
        //Thread.sleep(5*60*1000);
        //app.exit(applicationContext);
    }
}
