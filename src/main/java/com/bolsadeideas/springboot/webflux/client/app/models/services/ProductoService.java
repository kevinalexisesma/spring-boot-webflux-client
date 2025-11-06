package com.bolsadeideas.springboot.webflux.client.app.models.services;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

    public Flux<Producto> findAll();

    public Mono<Producto> findById(String id);

    public Mono<Producto> save(Producto save);

    public Mono<Producto> update(Producto save, String id);

    public Mono<Void> delete(String id);
}