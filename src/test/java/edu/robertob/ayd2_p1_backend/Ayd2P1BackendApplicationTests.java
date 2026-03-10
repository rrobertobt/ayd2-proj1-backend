package edu.robertob.ayd2_p1_backend;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Ayd2P1BackendApplicationTests {

	@Test
	void contextLoads() {
		assertNotNull(Ayd2P1BackendApplication.class);
	}

	@Test
	void main_callsSpringApplicationRun() {
		try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
			mocked.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
					.thenReturn(mock(ConfigurableApplicationContext.class));

			Ayd2P1BackendApplication.main(new String[]{});

			mocked.verify(() -> SpringApplication.run(Ayd2P1BackendApplication.class, new String[]{}));
		}
	}
}
