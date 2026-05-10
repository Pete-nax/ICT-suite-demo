package ke.co.kenit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;

@SpringBootApplication
@EnableScheduling
public class KenITApplication {

    public static void main(String[] args) {
        SpringApplication.run(KenITApplication.class, args);
    }

    // Virtual Threads — handles concurrent ticket submissions without thread-pool exhaustion
    // Crucial when the whole office submits tickets at 8am on a Monday
    @Bean
    public TomcatProtocolHandlerCustomizer<?> virtualThreadsCustomizer() {
        return protocolHandler ->
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
