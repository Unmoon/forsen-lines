package com.unmoon;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static net.runelite.api.ChatMessageType.GAMEMESSAGE;
import static net.runelite.client.util.Text.removeTags;

@Slf4j
@PluginDescriptor(
	name = "Forsen Lines"
)
public class ForsenLines extends Plugin
{
	@Inject
	private Client client;

	private static final List<String> death_sounds = Arrays.asList("deathScreen_respawn.wav", "deathScreen_title.wav", "death_attack_fall.wav", "death_attack_lava.wav", "death_attack_onFire.wav", "death_fell_accident_generic.wav");

	@Override
	protected void startUp() throws Exception
	{
		playSound("narrator_screen_title.wav");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGING_IN)
		{
			playSound("menu_game.wav");
		}
	}

	private void playSound(String sound_name)
	{
		InputStream stream = ForsenLines.class.getResourceAsStream(sound_name);
		if (stream == null) {
			log.warn("Failed to load sound '{}' (null)", sound_name);
			return;
		}
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(stream);
			Clip clip = AudioSystem.getClip();
			clip.open(sound);
			clip.loop(0);  // play once
		}
		catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
			log.warn("Failed to play sound '{}'", sound_name, e);
		}
	}

	public String getRandomElement(List<String> list)
	{
		Random rand = new Random();
		return list.get(rand.nextInt(list.size()));
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		Actor actor = event.getActor();
		if (actor instanceof Player) {
			Player player = (Player) actor;
			if (player == client.getLocalPlayer()) {
				// TODO: different messages for different bosses
				playSound(getRandomElement(death_sounds));
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != GAMEMESSAGE) return;
		String message = event.getMessage();
		switch (message)
		{
			case "You failed to survive the Tombs of Amascut.": playSound("menu_returnToMenu.wav"); return;
			case "You enter the Tombs of Amascut...": playSound("narrator_screen_title.wav"); return;
			case "You abandon the raid and leave the Tombs of Amascut.": playSound("menu_savingLevel.wav"); return;
			case "Challenge started: The Wardens": playSound("advancements_end_root_title.wav"); return;
			case "The statue has been struck! The seal weakens!": playSound("advancements_story_mine_stone_title.wav"); return;
		}

		if (message.startsWith("Challenge complete: Path of Het. Duration:")) playSound("advancements_story_upgrade_tools_title.wav");
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != 229) return;
		Widget toaLeaveDialog = client.getWidget(WidgetInfo.PACK(229, 1));
		if (toaLeaveDialog != null)
		{
			playSound("deathScreen_quit_confirm.wav");
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		String option = removeTags(event.getMenuOption() + event.getMenuTarget());
		switch (option)
		{
			case "Make Party": playSound("selectWorld_create.wav"); return;
			case "ActivateTry Again": playSound("options_difficulty_easy.wav"); return;
			case "ActivatePersistence": playSound("options_difficulty_normal.wav"); return;
			case "ActivateSoftcore Run": playSound("options_difficulty_hard.wav"); return;
			case "ActivateHardcore Run": playSound("options_difficulty_hardcore.wav"); return;
			case "ViewApplicants": playSound("menu_multiplayer.wav"); return;
			case "ViewInvocations": playSound("menu_options.wav"); return;
			case "Logout": playSound("menu_quit.wav"); return;
			case "InspectGrouping Obelisk": playSound("deathScreen_titleScreen.wav"); return;
			case "EnterEntry": playSound("selectWorld_enterName.wav"); return;
		}
	}
}
