package com.rotafolego.config;

import com.rotafolego.dto.PontoResponse;
import com.rotafolego.model.PontoDescanso;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // O JSON usa "tipo" (nome do tipo), mas o model guarda esse valor em "nmTipo".
        mapper.typeMap(PontoDescanso.class, PontoResponse.class)
              .addMapping(PontoDescanso::getNmTipo, PontoResponse::setTipo);

        return mapper;
    }
}
