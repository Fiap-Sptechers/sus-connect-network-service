package com.fiap.sus.network.modules.shift.dto;

public record ShiftFilter(
    String specialty,
    Boolean availableOnly // e.g. activeDoctors > 0
) {}
