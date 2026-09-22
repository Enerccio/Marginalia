package com.github.enerccio.marginalia.domain.service;

public record TurnInput(
    String sceneSetting,
    String povCharacter,
    String presentCharacters,
    String instructions
) {}