package br.ufrn.imd.loja.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.loja.dto.LojaDTO;
import br.ufrn.imd.loja.service.LojaService;

@RestController
@RequestMapping("/api/v1/loja")
public class LojaController {

  private final LojaService lojaService;

  public LojaController(LojaService lojaService) {
    this.lojaService = lojaService;
  }

  @PostMapping("/produto")
  public LojaDTO.ProdutoDTO cadastrarProduto(@RequestBody LojaDTO.CriarProdutoDTO criarProdutoDTO) 
  {
    return lojaService.cadastrarProduto(criarProdutoDTO);
  }

  @PutMapping("/produto/{codigo}/saida")
  public LojaDTO.ProdutoEstoqueDTO retirarEstoque(
    @PathVariable("codigo") final int codigo, @RequestBody final LojaDTO.RetirarEstoqueDTO retirarEstoqueDTO) 
  {
    return lojaService.retirarDoEstoque(codigo, retirarEstoqueDTO);
  }

}
