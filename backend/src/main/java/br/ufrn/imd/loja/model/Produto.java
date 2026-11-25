package br.ufrn.imd.loja.model;

import java.math.BigDecimal;

public class Produto
{
	private final int codigo;
	private final BigDecimal preco;
	private int estoque;

	public Produto(int codigo, BigDecimal preco, int estoque) 
	{
		this.codigo = codigo;
		this.preco = preco;
		this.estoque = estoque;
	}

	public Produto(int codigo, BigDecimal preco) 
	{
		this.codigo = codigo;
		this.preco = preco;
		this.estoque = 0;
	}

	public int getCodigo()
	{
		return codigo;
	}

	public BigDecimal getPreco()
	{
		return preco;
	}

	public int getEstoque()
	{
		return estoque;
	}

	public void setEstoque(int estoque)
	{
		this.estoque = estoque;
	}

}
