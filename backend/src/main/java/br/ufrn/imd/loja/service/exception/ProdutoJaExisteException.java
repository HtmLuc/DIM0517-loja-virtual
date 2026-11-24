package br.ufrn.imd.loja.service.exception;

public class ProdutoJaExisteException extends RuntimeException
{

	public ProdutoJaExisteException(int codigo)
	{
		super("Já existe produto com o código " + codigo);
	}

}
