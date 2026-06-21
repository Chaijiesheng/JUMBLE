package asia.fourtitude.interviewq.jumble.controller;

import java.time.ZonedDateTime;

import javax.validation.Valid;

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

import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import asia.fourtitude.interviewq.jumble.model.ExistsForm;
import asia.fourtitude.interviewq.jumble.model.PrefixForm;
import asia.fourtitude.interviewq.jumble.model.ScrambleForm;
import asia.fourtitude.interviewq.jumble.model.SearchForm;
import asia.fourtitude.interviewq.jumble.model.SubWordsForm;

@Controller
@RequestMapping(path = "/")
public class RootController {

    private static final Logger LOG = LoggerFactory.getLogger(RootController.class);

    private final JumbleEngine jumbleEngine;

    @Autowired(required = true)
    public RootController(JumbleEngine jumbleEngine) {
        this.jumbleEngine = jumbleEngine;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("timeNow", ZonedDateTime.now());
        return "index";
    }

    @GetMapping("scramble")
    public String doGetScramble(Model model) {
        model.addAttribute("form", new ScrambleForm());
        return "scramble";
    }

    @PostMapping("scramble")
    public String doPostScramble(
            @Valid @ModelAttribute(name = "form") ScrambleForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "scramble";
        }
        form.setScramble(jumbleEngine.scramble(form.getWord()));
        return "scramble";
    }

    @GetMapping("palindrome")
    public String doGetPalindrome(Model model) {
        model.addAttribute("words", this.jumbleEngine.retrievePalindromeWords());
        return "palindrome";
    }

    @GetMapping("exists")
    public String doGetExists(Model model) {
        model.addAttribute("form", new ExistsForm());
        return "exists";
    }

    @PostMapping("exists")
    public String doPostExists(
            @Valid @ModelAttribute(name = "form") ExistsForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "exists";
        }
        form.setExists(jumbleEngine.exists(form.getWord()));
        return "exists";
    }

    @GetMapping("prefix")
    public String doGetPrefix(Model model) {
        model.addAttribute("form", new PrefixForm());
        return "prefix";
    }

    @PostMapping("prefix")
    public String doPostPrefix(
            @Valid @ModelAttribute(name = "form") PrefixForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "prefix";
        }
        form.setWords(jumbleEngine.wordsMatchingPrefix(form.getPrefix()));
        return "prefix";
    }

    @GetMapping("search")
    public String doGetSearch(Model model) {
        model.addAttribute("form", new SearchForm());
        return "search";
    }

    @PostMapping("search")
    public String doPostSearch(
            @Valid @ModelAttribute(name = "form") SearchForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "search";
        }

        String startCharStr = form.getStartChar();
        String endCharStr = form.getEndChar();
        Integer length = form.getLength();

        boolean hasStart = startCharStr != null && !startCharStr.isEmpty();
        boolean hasEnd = endCharStr != null && !endCharStr.isEmpty();
        boolean hasLength = length != null && length >= 1;

        if (!hasStart && !hasEnd && !hasLength) {
            bindingResult.rejectValue("startChar", "invalid", "Invalid startChar");
            bindingResult.rejectValue("endChar", "invalid", "Invalid endChar");
            bindingResult.rejectValue("length", "invalid", "Invalid length");
            return "search";
        }

        Character startChar = hasStart ? startCharStr.charAt(0) : null;
        Character endChar = hasEnd ? endCharStr.charAt(0) : null;

        form.setWords(jumbleEngine.searchWords(startChar, endChar, length));
        return "search";
    }

    @GetMapping("subWords")
    public String goGetSubWords(Model model) {
        model.addAttribute("form", new SubWordsForm());
        return "subWords";
    }

    @PostMapping("subWords")
    public String doPostSubWords(
            @Valid @ModelAttribute(name = "form") SubWordsForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "subWords";
        }
        form.setWords(jumbleEngine.generateSubWords(form.getWord(), form.getMinLength()));
        return "subWords";
    }

}
