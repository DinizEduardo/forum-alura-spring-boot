package br.com.alura.forum.controller;

import br.com.alura.forum.controller.form.AtualizacaoTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.dto.DetalhesTopicoDTO;
import br.com.alura.forum.dto.TopicoDTO;
import br.com.alura.forum.modelo.Curso;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/topicos")
public class TopicosController {

    @Autowired
    private TopicoRepository topicoRepository;
    @Autowired
    private CursoRepository cursoRepository;

    @GetMapping
    @Cacheable(value = "listaDeTopicos")
    public Page<TopicoDTO> lista(@RequestParam(required = false) String nomeCurso,
                                 @PageableDefault(sort = "id",
                                         direction = Sort.Direction.ASC)
                                         Pageable paginacao) {

        Page<Topico> topicos;
        if(nomeCurso == null) {
             topicos = topicoRepository.findAll(paginacao);
        }else {
            topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);
        }


        return TopicoDTO.converter(topicos);
    }

    @PostMapping
    @Transactional
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder) {
        Topico topico = form.converter(cursoRepository);
        topicoRepository.save(topico);

        URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new TopicoDTO(topico));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhesTopicoDTO> detalhar(@PathVariable Long id) {

        Optional<Topico> topico = topicoRepository.findById(id);

        if(topico.isPresent()) {
            return ResponseEntity.ok(new DetalhesTopicoDTO(topico.get()));
        }

        return ResponseEntity.notFound().build();


    }

    // put   -> sobreescrever o recurso inteiro
    // patch -> alterar algo pequeno

    @PutMapping("/{id}")
    @Transactional
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> atualizar(@PathVariable Long id,
                                               @RequestBody @Valid AtualizacaoTopicoForm form) {

        Optional<Topico> topico = topicoRepository.findById(id);

        if(topico.isPresent()) {
            Topico topicoAtualizado = form.atualizar(id, topicoRepository);
            return ResponseEntity.ok(new TopicoDTO(topicoAtualizado));
        }



        return ResponseEntity.notFound().build();

    }

    @DeleteMapping("/{id}")
    @Transactional
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<?> deletar(@PathVariable Long id) {

        Optional<Topico> topico = topicoRepository.findById(id);

        if(topico.isPresent()) {
            topicoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();

    }


}
