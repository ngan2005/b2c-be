package com.example.B2C;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class B2CApplication {

	/**
	 * Spring Boot 4.x no longer honours the {@code META-INF/spring.factories}
	 * SPI for {@code EnvironmentPostProcessor} and the legacy
	 * {@code me.paulschwarz:spring-dotenv:4.0.0} (which the build still pulls
	 * in) registers a {@code SpringApplicationRunListener} whose
	 * {@code environmentPrepared} signature changed in SB 4.x, so it
	 * silently does nothing. We therefore load {@code .env} ourselves here
	 * and expose the entries as JVM system properties before Spring starts.
	 * Properties already set (e.g. via CI / shell env) take precedence.
	 */
	private static final Pattern LINE = Pattern.compile(
			"^\\s*(?:export\\s+)?([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.*?)\\s*$");

	public static void main(String[] args) {
		loadDotenvIntoSystemProperties();
		SpringApplication.run(B2CApplication.class, args);
	}

	private static void loadDotenvIntoSystemProperties() {
		Path envFile = Path.of(".env");
		if (!Files.exists(envFile)) {
			return;
		}
		try {
			for (String raw : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
				String line = stripComment(raw).trim();
				if (line.isEmpty()) continue;
				Matcher m = LINE.matcher(line);
				if (!m.matches()) continue;
				String key = m.group(1);
				String value = unquote(m.group(2));
				// Prefer an explicit shell / JVM property (CI / production).
				if (System.getProperty(key) != null) continue;
				System.setProperty(key, value);
			}
		} catch (IOException ex) {
			System.err.println("[B2C] Failed to load .env: " + ex.getMessage());
		}
	}

	private static String stripComment(String line) {
		int hash = -1;
		boolean inSingle = false;
		boolean inDouble = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '\'' && !inDouble) inSingle = !inSingle;
			else if (c == '"' && !inSingle) inDouble = !inDouble;
			else if (c == '#' && !inSingle && !inDouble) {
				// Treat '#' as a comment marker only when preceded by whitespace
				// or at the very start of the line. This preserves values such as
				// "DB_PASSWORD=foo!29s#bar" where the trailing '#' is part of the
				// secret, not a comment.
				if (i == 0 || Character.isWhitespace(line.charAt(i - 1))) {
					hash = i;
					break;
				}
			}
		}
		return hash < 0 ? line : line.substring(0, hash);
	}

	private static String unquote(String value) {
		if (value.length() >= 2) {
			char first = value.charAt(0);
			char last = value.charAt(value.length() - 1);
			if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
				return value.substring(1, value.length() - 1);
			}
		}
		return value;
	}
}
