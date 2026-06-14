package com.palmergames.bukkit.towny.hooks;

import com.astatine.eartopia.i18n.api.EartopiaI18n;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

final class EartopiaI18nLocaleBridgeTests {

	@Test
	void usesEartopiaEffectiveLocaleWhenServiceIsRegistered() {
		ServicesManager services = mock(ServicesManager.class);
		@SuppressWarnings("unchecked")
		RegisteredServiceProvider<EartopiaI18n> registration = mock(RegisteredServiceProvider.class);
		Player player = mock(Player.class);
		UUID playerId = UUID.randomUUID();

		when(player.getUniqueId()).thenReturn(playerId);
		when(player.locale()).thenReturn(Locale.US);
		when(services.getKnownServices()).thenReturn(Set.of(EartopiaI18n.class));
		when(services.getRegistration(EartopiaI18n.class)).thenReturn(registration);
		when(registration.getProvider()).thenReturn(new FakeI18n("ko_KR"));

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::getServicesManager).thenReturn(services);

			assertEquals(Locale.KOREA, EartopiaI18nLocaleBridge.getLocale(player, Locale.US));
		}
	}

	@Test
	void fallsBackToMinecraftLocaleWhenServiceIsNotRegistered() {
		ServicesManager services = mock(ServicesManager.class);
		Player player = mock(Player.class);

		when(player.locale()).thenReturn(Locale.JAPAN);
		when(services.getKnownServices()).thenReturn(Set.of());

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::getServicesManager).thenReturn(services);

			assertEquals(Locale.JAPAN, EartopiaI18nLocaleBridge.getLocale(player, Locale.US));
		}
	}

	@Test
	void fallsBackToMinecraftLocaleWhenServiceFails() {
		ServicesManager services = mock(ServicesManager.class);
		@SuppressWarnings("unchecked")
		RegisteredServiceProvider<EartopiaI18n> registration = mock(RegisteredServiceProvider.class);
		Player player = mock(Player.class);
		UUID playerId = UUID.randomUUID();

		when(player.getUniqueId()).thenReturn(playerId);
		when(player.locale()).thenReturn(Locale.JAPAN);
		when(services.getKnownServices()).thenReturn(Set.of(EartopiaI18n.class));
		when(services.getRegistration(EartopiaI18n.class)).thenReturn(registration);
		when(registration.getProvider()).thenReturn(new FailingI18n());

		try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
			bukkit.when(Bukkit::getServicesManager).thenReturn(services);

			assertEquals(Locale.JAPAN, EartopiaI18nLocaleBridge.getLocale(player, Locale.US));
		}
	}

	@Test
	void normalizesUnderscoreLocaleKeys() {
		assertEquals(Locale.TRADITIONAL_CHINESE, EartopiaI18nLocaleBridge.toLocale("zh_TW", Locale.US));
	}

	public static final class FakeI18n implements EartopiaI18n {
		private final FakeLocales locales;

		FakeI18n(String localeKey) {
			this.locales = new FakeLocales(localeKey);
		}

		public FakeLocales locales() {
			return locales;
		}
	}

	public static final class FailingI18n implements EartopiaI18n {
		public FakeLocales locales() {
			throw new IllegalStateException("boom");
		}
	}

	public static final class FakeLocales {
		private final String localeKey;

		FakeLocales(String localeKey) {
			this.localeKey = localeKey;
		}

		public String effectiveLocaleKey(UUID playerId, Locale minecraftLocale) {
			return localeKey;
		}
	}
}
