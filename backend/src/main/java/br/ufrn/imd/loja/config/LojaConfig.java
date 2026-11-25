package br.ufrn.imd.loja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.ufrn.imd.loja.repository.ProdutoRepository;
import br.ufrn.imd.loja.repository.ProdutoRepositoryImpl;
import br.ufrn.imd.loja.service.LojaService;

@Configuration
public class LojaConfig
{

	@Bean
	public ProdutoRepository produtoRepository()
	{
		return new ProdutoRepositoryImpl();
	}

	@Bean
	public LojaService lojaService(ProdutoRepository produtoRepository)
	{
		return new LojaService(produtoRepository);
	}

}
