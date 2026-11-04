package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * What a {@link LexiconEntry} is, separate from its category (a semantic group) and from the part of speech of a sense, which a
 * language without word classes blurs on purpose.
 */
public enum EntryKind {
    ROOT, COMPOUND, AFFIX, PARTICLE, NAME, NUMBER, PHRASE, OTHER
}
