package com.github.enerccio.marginalia.domain.model.impl;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "protocols_chatcompletion")
public class ChatCompletionProtocol extends Protocol {

}