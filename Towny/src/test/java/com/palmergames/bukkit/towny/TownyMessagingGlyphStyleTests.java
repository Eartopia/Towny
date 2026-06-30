package com.palmergames.bukkit.towny;

import com.palmergames.bukkit.util.Colors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TownyMessagingGlyphStyleTests {

	@Test
	void defaultBodyColorDoesNotApplyToUiGlyph() {
		String message = "<font:eartopia:ui>\\uE721</font> Notice";

		assertEquals("<green><reset><font:eartopia:ui>\\uE721</font><green> Notice",
			TownyMessaging.withDefaultBodyColor(Colors.GREEN, message));
	}

	@Test
	void defaultBodyColorResumesAfterEachUiGlyph() {
		String message = "A <font:eartopia:ui>\\uE716</font> B <font:eartopia:ui>\\uE717</font> C";

		assertEquals("<green>A <reset><font:eartopia:ui>\\uE716</font><green> B <reset><font:eartopia:ui>\\uE717</font><green> C",
			TownyMessaging.withDefaultBodyColor(Colors.GREEN, message));
	}

	@Test
	void defaultBodyColorLeavesPlainMessagesUntouched() {
		assertEquals("<green>Hello", TownyMessaging.withDefaultBodyColor(Colors.GREEN, "Hello"));
	}
}
