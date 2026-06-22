package tinyswordsisland.controller;

import tinyswordsisland.view.GameViewState;

import java.util.List;

import tinyswordsisland.model.event.AudioEventType;

public interface IController {
    GameViewState snapshot();
    List<AudioEventType> consumeAudioEvents();
}
