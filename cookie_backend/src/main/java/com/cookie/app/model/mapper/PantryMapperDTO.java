package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.PantryDTO;
import com.cookie.app.model.entity.Pantry;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class PantryMapperDTO implements Function<Pantry, PantryDTO> {
    @Override
    public PantryDTO apply(Pantry pantry) {
        return new PantryDTO(
                pantry.getId(),
                pantry.getPantryName(),
                pantry.getPantryProducts().size(),
                pantry.getGroup().getId(),
                pantry.getGroup().getGroupName()
        );
    }
}
