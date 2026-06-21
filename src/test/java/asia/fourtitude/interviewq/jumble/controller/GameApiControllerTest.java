package asia.fourtitude.interviewq.jumble.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import asia.fourtitude.interviewq.jumble.TestConfig;
import asia.fourtitude.interviewq.jumble.core.JumbleEngine;

@WebMvcTest(GameApiController.class)
@Import(TestConfig.class)
class GameApiControllerTest {

    static final ObjectMapper OM = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    JumbleEngine jumbleEngine;

    private Map<String, Object> createNewGame() throws Exception {
        MvcResult result = this.mvc.perform(get("/api/game/new"))
                .andExpect(status().isOk())
                .andReturn();
        return OM.readValue(result.getResponse().getContentAsString(), Map.class);
    }

    @Test
    void whenCreateNewGame_thenSuccess() throws Exception {
        Map<String, Object> resp = createNewGame();

        assertEquals("Created new game.", resp.get("result"));
        assertNotNull(resp.get("id"));
        assertNotNull(resp.get("original_word"));
        assertNotNull(resp.get("scramble_word"));
        int totalWords = (int) resp.get("total_words");
        int remainingWords = (int) resp.get("remaining_words");
        assertTrue(totalWords > 0);
        assertTrue(remainingWords > 0);
        assertEquals(totalWords, remainingWords);
        List<?> guessedWords = (List<?>) resp.get("guessed_words");
        assertNotNull(guessedWords);
        assertTrue(guessedWords.isEmpty());
    }

    @Test
    void givenMissingId_whenPlayGame_thenInvalidId() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        // no id field

        MvcResult result = this.mvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OM.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andReturn();
        Map<String, Object> resp = OM.readValue(result.getResponse().getContentAsString(), Map.class);
        assertEquals("Invalid Game ID.", resp.get("result"));
    }

    @Test
    void givenMissingRecord_whenPlayGame_thenRecordNotFound() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", UUID.randomUUID().toString());
        // no word field

        MvcResult result = this.mvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OM.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andReturn();
        Map<String, Object> resp = OM.readValue(result.getResponse().getContentAsString(), Map.class);
        assertEquals("Game board/state not found.", resp.get("result"));
    }

    @Test
    void givenCreateNewGame_whenSubmitNullWord_thenGuessedIncorrectly() throws Exception {
        Map<String, Object> newResp = createNewGame();
        String gameId = (String) newResp.get("id");
        String originalWord = (String) newResp.get("original_word");
        int totalWords = (int) newResp.get("total_words");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", gameId);
        // no word field (null)

        MvcResult result = this.mvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OM.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> resp = OM.readValue(result.getResponse().getContentAsString(), Map.class);

        assertEquals("Guessed incorrectly.", resp.get("result"));
        assertEquals(gameId, resp.get("id"));
        assertEquals(originalWord, resp.get("original_word"));
        assertNotNull(resp.get("scramble_word"));
        assertNull(resp.get("guess_word"));
        assertEquals(totalWords, resp.get("total_words"));
        assertEquals(totalWords, resp.get("remaining_words"));
        List<?> guessedWords = (List<?>) resp.get("guessed_words");
        assertNotNull(guessedWords);
        assertTrue(guessedWords.isEmpty());
    }

    @Test
    void givenCreateNewGame_whenSubmitWrongWord_thenGuessedIncorrectly() throws Exception {
        Map<String, Object> newResp = createNewGame();
        String gameId = (String) newResp.get("id");
        String originalWord = (String) newResp.get("original_word");
        int totalWords = (int) newResp.get("total_words");

        String wrongWord = "xxxxxxxxxx"; // 10 chars, can't be a sub-word of a 6-letter word

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", gameId);
        requestBody.put("word", wrongWord);

        MvcResult result = this.mvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OM.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> resp = OM.readValue(result.getResponse().getContentAsString(), Map.class);

        assertEquals("Guessed incorrectly.", resp.get("result"));
        assertEquals(gameId, resp.get("id"));
        assertEquals(originalWord, resp.get("original_word"));
        assertNotNull(resp.get("scramble_word"));
        assertEquals(wrongWord, resp.get("guess_word"));
        assertEquals(totalWords, resp.get("total_words"));
        assertEquals(totalWords, resp.get("remaining_words"));
        List<?> guessedWords = (List<?>) resp.get("guessed_words");
        assertNotNull(guessedWords);
        assertTrue(guessedWords.isEmpty());
    }

    @Test
    void givenCreateNewGame_whenSubmitFirstCorrectWord_thenGuessedCorrectly() throws Exception {
        Map<String, Object> newResp = createNewGame();
        String gameId = (String) newResp.get("id");
        String originalWord = (String) newResp.get("original_word");
        int totalWords = (int) newResp.get("total_words");

        Collection<String> subWords = jumbleEngine.generateSubWords(originalWord, 3);
        assertTrue(subWords.size() >= 1, "must have at least 1 sub-word");
        String correctWord = subWords.iterator().next();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", gameId);
        requestBody.put("word", correctWord);

        MvcResult result = this.mvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OM.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> resp = OM.readValue(result.getResponse().getContentAsString(), Map.class);

        assertEquals("Guessed correctly.", resp.get("result"));
        assertEquals(gameId, resp.get("id"));
        assertEquals(originalWord, resp.get("original_word"));
        assertNotNull(resp.get("scramble_word"));
        assertEquals(correctWord, resp.get("guess_word"));
        assertEquals(totalWords, resp.get("total_words"));
        assertEquals(totalWords - 1, resp.get("remaining_words"));
        List<?> guessedWords = (List<?>) resp.get("guessed_words");
        assertNotNull(guessedWords);
        assertFalse(guessedWords.isEmpty());
        assertTrue(guessedWords.contains(correctWord));
    }

    @Test
    void givenCreateNewGame_whenSubmitAllCorrectWord_thenAllGuessed() throws Exception {
        Map<String, Object> newResp = createNewGame();
        String gameId = (String) newResp.get("id");
        String originalWord = (String) newResp.get("original_word");
        int totalWords = (int) newResp.get("total_words");

        List<String> correctWords = new ArrayList<>(jumbleEngine.generateSubWords(originalWord, 3));
        assertTrue(correctWords.size() >= 1, "must have at least 1 sub-word");

        // Submit all words except last
        for (int i = 0; i < correctWords.size() - 1; i++) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", gameId);
            requestBody.put("word", correctWords.get(i));
            this.mvc.perform(post("/api/game/guess")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OM.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());
        }

        // Submit the last word
        String lastWord = correctWords.get(correctWords.size() - 1);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", gameId);
        requestBody.put("word", lastWord);

        MvcResult result = this.mvc.perform(post("/api/game/guess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OM.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> resp = OM.readValue(result.getResponse().getContentAsString(), Map.class);

        assertEquals("All words guessed.", resp.get("result"));
        assertEquals(gameId, resp.get("id"));
        assertEquals(originalWord, resp.get("original_word"));
        assertNotNull(resp.get("scramble_word"));
        assertEquals(lastWord, resp.get("guess_word"));
        assertEquals(totalWords, resp.get("total_words"));
        assertEquals(0, resp.get("remaining_words"));
        List<?> guessedWords = (List<?>) resp.get("guessed_words");
        assertNotNull(guessedWords);
        assertFalse(guessedWords.isEmpty());
        assertTrue(guessedWords.contains(lastWord));
    }

}
