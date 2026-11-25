package br.ufrn.imd.loja.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.loja.dto.LojaDTO;
import br.ufrn.imd.loja.service.LojaService;

@RestController("/api/v1/loja")
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
}
