package fr.sacquet.covid.model.rest;

import lombok.*;

import java.util.Map;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class TrancheAge {
    String indice;
    String label;
    String color;
    Map<String, Integer> data;
    Map<String, Integer> dataP;
}
