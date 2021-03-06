package sample;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.server.test.ConfigServerTestUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
		properties = "spring.application.name:bad", webEnvironment = RANDOM_PORT)
public class ServerNativeApplicationTests {

	private static int configPort = 0;

	@Autowired
	private ConfigurableEnvironment environment;

	@LocalServerPort
	private int port;

	private static ConfigurableApplicationContext server;

	@BeforeClass
	public static void startConfigServer() throws IOException {
		String repo = ConfigServerTestUtils.prepareLocalRepo();
		server = SpringApplication.run(
				org.springframework.cloud.config.server.ConfigServerApplication.class,
				"--server.port=" + configPort, "--spring.config.name=server",
				"--spring.cloud.config.server.git.uri=" + repo, "--spring.profiles.active=native");
		configPort = ((EmbeddedWebApplicationContext) server)
				.getEmbeddedServletContainer().getPort();
		System.setProperty("config.port", "" + configPort);
	}

	@AfterClass
	public static void close() {
		System.clearProperty("config.port");
		if (server!=null) {
			server.close();
		}
	}

	@Test
	public void contextLoads() {
		// The remote config was bad so there is no bootstrap
		assertFalse(this.environment.getPropertySources().contains("bootstrap"));
	}

	public static void main(String[] args) throws IOException {
		configPort = 8888;
		startConfigServer();
		SpringApplication.run(Application.class, args);
	}

}
