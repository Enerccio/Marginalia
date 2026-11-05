package com.github.enerccio.marginalia.workset;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * The skeleton of the story (beats and timeline) as written by {@code work/dataset/build_skeleton.py}: {@code beats.json}.
 */
public class Skeleton {

    public record Beat(int n, int day, String time, String length, int[] words, String pov, String scene, String present,
                       List<String> languages, String instructions) {

        public Instant at(LocalDate start) {
            return start.plusDays(day - 1L).atTime(LocalTime.parse(time)).toInstant(ZoneOffset.UTC);
        }
    }

    public record BranchPart(String length, String pov, String scene, String present, List<String> languages, String instructions) {
    }

    public record Branch(int fromPart, List<BranchPart> parts) {
    }

    public String title;
    public String description;
    public List<Beat> beats;
    public Branch branch;

    public static Skeleton load(Path beatsJson) throws IOException {
        return new Gson().fromJson(Files.readString(beatsJson, StandardCharsets.UTF_8), Skeleton.class);
    }

    /** The instructions of a beat as the author would type them, with the length the beat asks for (times {@code factor}: the model writes longer than asked). */
    public static String instructions(String instructions, int[] words, boolean lengthHint, double factor) {
        if (!lengthHint || words == null || words.length < 2) {
            return instructions;
        }
        return instructions + " About " + Math.round((words[0] + words[1]) / 2.0 * factor / 10) * 10 + " words.";
    }
}
