package dev.creoii.dungeoneer.client.command;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.network.c2s.ExecuteCommandC2S;

import java.util.Arrays;

public class ClientCommandManager extends InputAdapter {
    private static final int TEXT_PADDING = 10;
    private final Dungeoneer client;
    private final StringBuilder input = new StringBuilder();
    private boolean active = false;
    private boolean suppressNextChar = false;

    public ClientCommandManager(Dungeoneer client) {
        this.client = client;
    }

    public Dungeoneer getClient() {
        return client;
    }

    public StringBuilder getInput() {
        return input;
    }

    public boolean isActive() {
        return active && client.getState().getStatus().isLoaded();
    }

    public void executeCommand(String command) {
        String[] elements = command.split(" ");
        String commandType = elements[0].substring(1);

        if (commandType.isEmpty())
            return;

        String[] args = Arrays.copyOfRange(elements, 1, elements.length);
        long raidId = client.getState().getCurrentRaid() == null || client.getState().getCurrentRaid().isNull() ? -1 : client.getState().getCurrentRaid().get().id();
        client.get().sendTCP(new ExecuteCommandC2S(client.getState().getAccount().id(), raidId, commandType, args));
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!isActive()) {
            if (keycode == client.getSettings().commandKey().value()) {
                active = true;
                input.setLength(0);
                input.append('/');
                suppressNextChar = true;
                return true;
            }
            return false;
        }

        if (keycode == Input.Keys.ESCAPE) {
            input.setLength(0);
            active = false;
        } else if (keycode == Input.Keys.ENTER) {
            if (!input.isEmpty()) {
                if (input.charAt(0) == '/') {
                    executeCommand(input.toString());
                }
            }
            input.setLength(0);
            active = false;
        }

        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        if (!isActive())
            return false;

        if (suppressNextChar) {
            suppressNextChar = false;
            return true;
        }

        if (character == '\b') {
            if (!input.isEmpty())
                input.deleteCharAt(input.length() - 1);
        } else {
            input.append(character);
            return true;
        }

        return false;
    }

    public void render(AbstractScreen abstractScreen) {
        Assets.FONT.setColor(Color.WHITE);
        Assets.FONT.draw(abstractScreen.getStage().getBatch(), "> " + input + ((System.currentTimeMillis() / 400) % 2 == 0 ? "_" : ""), TEXT_PADDING, Assets.FONT.getCapHeight() + TEXT_PADDING);
        Assets.FONT.setColor(Color.LIGHT_GRAY);
    }
}
