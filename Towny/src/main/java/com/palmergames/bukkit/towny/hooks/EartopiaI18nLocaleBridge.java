package com.palmergames.bukkit.towny.hooks;

import com.palmergames.bukkit.towny.Towny;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EartopiaI18nLocaleBridge {
	private static final String SERVICE_CLASS_NAME = "com.astatine.eartopia.i18n.api.EartopiaI18n";
	private static final AtomicBoolean warnedReflectionFailure = new AtomicBoolean();

	private EartopiaI18nLocaleBridge() {
	}

	public static Locale getLocale(Player player, Locale fallbackLocale) {
		Locale minecraftLocale = player.locale();
		String eartopiaLocaleKey = effectiveLocaleKey(player.getUniqueId(), minecraftLocale);
		if (eartopiaLocaleKey == null || eartopiaLocaleKey.isBlank()) {
			return minecraftLocale == null ? fallbackLocale : minecraftLocale;
		}

		return toLocale(eartopiaLocaleKey, fallbackLocale);
	}

	static Locale toLocale(String localeKey, Locale fallbackLocale) {
		if (localeKey == null || localeKey.isBlank()) {
			return fallbackLocale;
		}

		Locale locale = Locale.forLanguageTag(localeKey.replace('_', '-'));
		if (locale.getLanguage().isBlank()) {
			return fallbackLocale;
		}
		return locale;
	}

	private static String effectiveLocaleKey(UUID playerId, Locale minecraftLocale) {
		try {
			for (Class<?> serviceClass : Bukkit.getServicesManager().getKnownServices()) {
				if (!SERVICE_CLASS_NAME.equals(serviceClass.getName())) {
					continue;
				}

				RegisteredServiceProvider<?> registration = Bukkit.getServicesManager().getRegistration(serviceClass);
				if (registration == null) {
					return null;
				}

				Object provider = registration.getProvider();
				Object localeService = provider.getClass().getMethod("locales").invoke(provider);
				Object localeKey = localeService.getClass()
					.getMethod("effectiveLocaleKey", UUID.class, Locale.class)
					.invoke(localeService, playerId, minecraftLocale);
				return localeKey instanceof String string ? string : null;
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | RuntimeException | LinkageError exception) {
			warnReflectionFailure(exception);
		}
		return null;
	}

	private static void warnReflectionFailure(Throwable throwable) {
		if (warnedReflectionFailure.compareAndSet(false, true)) {
			String message = "Could not read EartopiaI18n locale service; falling back to Minecraft client locales. Cause: " + throwable.getClass().getSimpleName();
			try {
				Towny.getPlugin().getLogger().warning(message);
			} catch (RuntimeException exception) {
				try {
					Bukkit.getLogger().warning(message);
				} catch (RuntimeException ignored) {
					// Bukkit may be unavailable in isolated unit tests.
				}
			}
		}
	}
}
