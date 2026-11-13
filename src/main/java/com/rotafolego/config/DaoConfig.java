package com.rotafolego.config;

import com.rotafolego.dao.AvaliacaoDao;
import com.rotafolego.dao.PontoDescansoDao;
import com.rotafolego.dao.TipoPontoDao;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cria os DAOs entregando o DataSource (pool Hikari com o Oracle) pelo construtor. */
@Configuration
public class DaoConfig {

    @Bean
    public TipoPontoDao tipoPontoDao(DataSource dataSource) {
        return new TipoPontoDao(dataSource);
    }

    @Bean
    public PontoDescansoDao pontoDescansoDao(DataSource dataSource) {
        return new PontoDescansoDao(dataSource);
    }

    @Bean
    public AvaliacaoDao avaliacaoDao(DataSource dataSource) {
        return new AvaliacaoDao(dataSource);
    }
}
