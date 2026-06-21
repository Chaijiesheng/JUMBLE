package asia.fourtitude.interviewq.jumble.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import asia.fourtitude.interviewq.jumble.core.GameState;
import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import asia.fourtitude.interviewq.jumble.model.GameBoard;

@Controller
@RequestMapping(path = "/game")
@SessionAttributes("board")
public class GameWebController {

    private static final Logger LOG = LoggerFactory.getLogger(GameWebController.class);

    private final JumbleEngine jumbleEngine;

    @Autowired(required = true)
    public GameWebController(JumbleEngine jumbleEngine) {
        this.jumbleEngine = jumbleEngine;
    }

    @ModelAttribute("board")
    public GameBoard gameBoard() {
        /*
         * This method with "@ModelAttribute" annotation, is so that
         * Spring can create/initialize an attribute into session scope.
         */
        return new GameBoard();
    }

    private void scrambleWord(GameBoard board) {
        if (board.getState() != null) {
            String oldScramble = board.getState().getScramble();
            int num = 0;
            do {
                String scramble = this.jumbleEngine.scramble(board.getState().getOriginal());
                board.getState().setScramble(scramble);
                num += 1;
            } while (oldScramble.equals(board.getState().getScramble()) && num <= 10);
        }
    }

    @GetMapping(path = "/goodbye")
    public String goodbye(SessionStatus status) {
        status.setComplete();
        return "game/board";
    }

    @GetMapping("/help")
    public String doGetHelp() {
        return "game/help";
    }

    @GetMapping("/new")
    public String doGetNew(@ModelAttribute(name = "board") GameBoard board) {
        GameState state = this.jumbleEngine.createGameState(6, 3);
        board.setState(state);
        board.setWord("");
        return "game/board";
    }

    @GetMapping("/play")
    public String doGetPlay(@ModelAttribute(name = "board") GameBoard board) {
        scrambleWord(board);

        return "game/board";
    }

    @PostMapping("/play")
    public String doPostPlay(
            @ModelAttribute(name = "board") GameBoard board,
            BindingResult bindingResult, Model model) {
        if (board == null || board.getState() == null) {
            // session expired
            return "game/board";
        }

        scrambleWord(board);

        String word = board.getWord();
        if (word != null) {
            word = word.trim().toLowerCase();
        }
        boolean guessed = board.getState().updateGuessWord(word);
        if (!guessed) {
            model.addAttribute("message", "Guessed incorrectly");
        }
        board.setWord("");

        return "game/board";
    }

}
