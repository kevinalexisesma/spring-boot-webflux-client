package com.bolsadeideas.springboot.webflux.client.app.models.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private WebClient client;

    @Override
    public Flux<Producto> findAll() {
        return client.get().accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(Producto.class));
    }

    @Override
    public Mono<Producto> findById(String id) {
        return client.get().uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Producto> save(Producto save) {
        return null;
    }

    @Override
    public Mono<Producto> update(Producto save, String id) {
        return null;
    }

    @Override
    public Mono<Void> delete(String id) {
        return null;
    }

}
